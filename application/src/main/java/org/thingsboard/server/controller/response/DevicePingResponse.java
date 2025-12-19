package org.thingsboard.server.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevicePingResponse {

    private boolean online;
    private long lastSeen;
    private String message;

}
