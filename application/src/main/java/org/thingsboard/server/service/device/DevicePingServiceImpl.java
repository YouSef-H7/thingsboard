package org.thingsboard.server.service.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.response.DevicePingResponse;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.Optional;

@Service
@TbCoreComponent
@Slf4j
@RequiredArgsConstructor
public class DevicePingServiceImpl implements DevicePingService {

    private final DeviceService deviceService;

    @Override
    public DevicePingResponse pingDevice(TenantId tenantId, DeviceId deviceId) {
        try {
            Device device = deviceService.findDeviceById(tenantId, deviceId);
            if (device == null) {
                return new DevicePingResponse(false, 0L, "Device not found");
            }
            boolean online = isDeviceOnline(device);
            long lastSeen = device.getCreatedTime(); // Using creation time as proxy for last activity
            String status = online ? "Device is online" : "Device is offline";
            return new DevicePingResponse(online, lastSeen, status);
        } catch (Exception e) {
            log.error("Error pinging device: {}", deviceId, e);
            return new DevicePingResponse(false, 0L, "Error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Device> getDevice(TenantId tenantId, DeviceId deviceId) {
        try {
            Device device = deviceService.findDeviceById(tenantId, deviceId);
            return Optional.ofNullable(device);
        } catch (Exception e) {
            log.error("Error retrieving device: {}", deviceId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isDeviceOnline(Device device) {
        if (device == null) {
            return false;
        }
        // NOTE: In ThingsBoard, proper device state tracking requires DeviceStateService.
        // This simple implementation returns false for all devices.
        // For production, integrate with DeviceStateService to check actual connectivity.
        // For now, we'll assume devices reported recently are likely online based on creation time.
        long currentTime = System.currentTimeMillis();
        long createdTime = device.getCreatedTime();
        // Consider device online if created within last 5 minutes (simplified heuristic)
        return (currentTime - createdTime) < (5 * 60 * 1000);
    }
}
