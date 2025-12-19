package org.thingsboard.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.controller.response.DevicePingResponse;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.device.DevicePingService;

import java.util.Optional;
import java.util.UUID;

import static org.thingsboard.server.controller.ControllerConstants.DEVICE_ID;
import static org.thingsboard.server.controller.ControllerConstants.DEVICE_ID_PARAM_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH;

@Slf4j
@RestController
@TbCoreComponent
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Device Ping Controller", description = "Controller for pinging devices")
public class DevicePingController extends BaseController {

    private final DevicePingService devicePingService;

    @Operation(summary = "Ping device (pingDevice)",
            description = "Check device connectivity by attempting to ping it. " +
                    "Returns whether the device is online and the last time it was active. " +
                    TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping("/device/{deviceId}/ping")
    public ResponseEntity<DevicePingResponse> pingDevice(
            @Parameter(description = DEVICE_ID_PARAM_DESCRIPTION)
            @PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        try {
            checkParameter(DEVICE_ID, strDeviceId);
            var tenantId = getTenantId();
            var deviceId = new DeviceId(toUUID(strDeviceId));

            // Verify device exists and user has access
            var deviceOpt = devicePingService.getDevice(tenantId, deviceId);
            if (deviceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Device device = deviceOpt.get();
            DevicePingResponse response = devicePingService.pingDevice(tenantId, deviceId);
            return ResponseEntity.ok(response);

        } catch (ThingsboardException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ping failed for device {}: {}", strDeviceId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new DevicePingResponse(false, 0L, "Ping error: " + e.getMessage()));
        }
    }
}
