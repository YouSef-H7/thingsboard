/*
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.service.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.response.DevicePingResponse;
import org.thingsboard.server.dao.device.DeviceService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DevicePingServiceImpl
 *
 * Tests verify core Ping functionality without requiring Docker or database connectivity.
 * All dependencies are mocked using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class DevicePingServiceImplTest {

    private DevicePingServiceImpl devicePingService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private Device mockDevice;

    private TenantId tenantId;
    private DeviceId deviceId;

    @BeforeEach
    void setUp() {
        devicePingService = new DevicePingServiceImpl(deviceService);
        tenantId = new TenantId(UUID.randomUUID());
        deviceId = new DeviceId(UUID.randomUUID());
    }

    /**
     * Test: Device online scenario
     * Verifies that pingDevice returns online=true when device is recently created
     */
    @Test
    void testPingDeviceOnline() {
        // Arrange
        long recentCreatedTime = System.currentTimeMillis() - (2 * 60 * 1000); // 2 minutes ago
        when(mockDevice.getCreatedTime()).thenReturn(recentCreatedTime);
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(mockDevice);

        // Act
        DevicePingResponse response = devicePingService.pingDevice(tenantId, deviceId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isOnline(), "Device should be online (created within 5 minutes)");
        assertEquals(recentCreatedTime, response.getLastSeen(), "Last seen should match device creation time");
        assertNotNull(response.getMessage(), "Message should not be null");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: Device offline scenario
     * Verifies that pingDevice returns online=false when device was created long ago
     */
    @Test
    void testPingDeviceOffline() {
        // Arrange
        long oldCreatedTime = System.currentTimeMillis() - (10 * 60 * 1000); // 10 minutes ago
        when(mockDevice.getCreatedTime()).thenReturn(oldCreatedTime);
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(mockDevice);

        // Act
        DevicePingResponse response = devicePingService.pingDevice(tenantId, deviceId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isOnline(), "Device should be offline (created more than 5 minutes ago)");
        assertEquals(oldCreatedTime, response.getLastSeen(), "Last seen should match device creation time");
        assertNotNull(response.getMessage(), "Message should not be null");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: Device not found scenario
     * Verifies that pingDevice returns online=false when device doesn't exist
     */
    @Test
    void testPingDeviceNotFound() {
        // Arrange
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(null);

        // Act
        DevicePingResponse response = devicePingService.pingDevice(tenantId, deviceId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isOnline(), "Response should indicate device is offline when not found");
        assertEquals(0L, response.getLastSeen(), "LastSeen should be 0 when device not found");
        assertTrue(response.getMessage().contains("not found"), "Message should indicate device not found");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: isDeviceOnline within 5-minute window
     * Verifies that device is considered online if created within last 5 minutes
     */
    @Test
    void testIsDeviceOnlineWithinTimeWindow() {
        // Arrange
        long timeWithinWindow = System.currentTimeMillis() - (3 * 60 * 1000); // 3 minutes ago
        when(mockDevice.getCreatedTime()).thenReturn(timeWithinWindow);

        // Act
        boolean isOnline = devicePingService.isDeviceOnline(mockDevice);

        // Assert
        assertTrue(isOnline, "Device should be online when created within 5-minute window");
    }

    /**
     * Test: isDeviceOffline outside 5-minute window
     * Verifies that device is considered offline if created more than 5 minutes ago
     */
    @Test
    void testIsDeviceOfflineOutsideTimeWindow() {
        // Arrange
        long timeOutsideWindow = System.currentTimeMillis() - (6 * 60 * 1000); // 6 minutes ago
        when(mockDevice.getCreatedTime()).thenReturn(timeOutsideWindow);

        // Act
        boolean isOnline = devicePingService.isDeviceOnline(mockDevice);

        // Assert
        assertFalse(isOnline, "Device should be offline when created outside 5-minute window");
    }

    /**
     * Test: isDeviceOnline with null device
     * Verifies that isDeviceOnline returns false for null device
     */
    @Test
    void testIsDeviceOnlineWithNullDevice() {
        // Act
        boolean isOnline = devicePingService.isDeviceOnline(null);

        // Assert
        assertFalse(isOnline, "isDeviceOnline should return false for null device");
    }

    /**
     * Test: getDevice when device exists
     * Verifies that getDevice returns Optional with device when found
     */
    @Test
    void testGetDeviceWhenExists() {
        // Arrange
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(mockDevice);

        // Act
        Optional<Device> result = devicePingService.getDevice(tenantId, deviceId);

        // Assert
        assertTrue(result.isPresent(), "Optional should contain device");
        assertEquals(mockDevice, result.get(), "Should return the mocked device");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: getDevice when device not exists
     * Verifies that getDevice returns empty Optional when device not found
     */
    @Test
    void testGetDeviceWhenNotExists() {
        // Arrange
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(null);

        // Act
        Optional<Device> result = devicePingService.getDevice(tenantId, deviceId);

        // Assert
        assertFalse(result.isPresent(), "Optional should be empty when device not found");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: getDevice with exception handling
     * Verifies that getDevice returns empty Optional when exception occurs
     */
    @Test
    void testGetDeviceWithException() {
        // Arrange
        when(deviceService.findDeviceById(tenantId, deviceId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        Optional<Device> result = devicePingService.getDevice(tenantId, deviceId);

        // Assert
        assertFalse(result.isPresent(), "Optional should be empty when exception occurs");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: pingDevice with exception handling
     * Verifies that pingDevice returns error response when exception occurs
     */
    @Test
    void testPingDeviceWithException() {
        // Arrange
        when(deviceService.findDeviceById(tenantId, deviceId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        DevicePingResponse response = devicePingService.pingDevice(tenantId, deviceId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isOnline(), "Device should be offline in error scenario");
        assertEquals(0L, response.getLastSeen(), "LastSeen should be 0 in error scenario");
        assertTrue(response.getMessage().contains("Error"), "Message should indicate error");

        // Verify
        verify(deviceService, times(1)).findDeviceById(tenantId, deviceId);
    }

    /**
     * Test: boundary condition - exactly 5 minutes
     * Verifies behavior at exactly 5-minute boundary
     */
    @Test
    void testIsDeviceOnlineAtBoundary() {
        // Arrange - exactly 5 minutes ago
        long exactBoundary = System.currentTimeMillis() - (5 * 60 * 1000);
        when(mockDevice.getCreatedTime()).thenReturn(exactBoundary);

        // Act
        boolean isOnline = devicePingService.isDeviceOnline(mockDevice);

        // Assert
        // Device at exactly 5 minutes should be considered online (< check is exclusive of upper bound)
        assertTrue(isOnline, "Device at exactly 5-minute boundary should be online");
    }
}
