package org.thingsboard.server.service.device;

import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.response.DevicePingResponse;

import java.util.Optional;

public interface DevicePingService {
    /**
     * Ping a device and return its online status
     *
     * @param tenantId the tenant ID
     * @param deviceId the device ID
     * @return DevicePingResponse with online status and last activity time
     */
    DevicePingResponse pingDevice(TenantId tenantId, DeviceId deviceId);
    
    /**
     * Get device by tenant and device ID
     *
     * @param tenantId the tenant ID
     * @param deviceId the device ID
     * @return Optional containing the Device or empty if not found
     */
    Optional<Device> getDevice(TenantId tenantId, DeviceId deviceId);
    
    /**
     * Check if device is currently online
     *
     * @param device the device to check
     * @return true if device is online, false otherwise
     */
    boolean isDeviceOnline(Device device);
}
