package org.rocs.asa.service.device.token;

import org.rocs.asa.domain.device.token.DeviceToken;

public interface DeviceTokenService {

    DeviceToken registerDeviceToken(DeviceToken deviceToken);

    DeviceToken removeDeviceToken(Long userId, String fcmToken);

}
