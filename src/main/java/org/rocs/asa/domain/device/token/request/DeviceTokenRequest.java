package org.rocs.asa.domain.device.token.request;

import lombok.Data;

@Data
public class DeviceTokenRequest {
    private String userId;
    private String fcmToken;
    private String deviceType;
}
