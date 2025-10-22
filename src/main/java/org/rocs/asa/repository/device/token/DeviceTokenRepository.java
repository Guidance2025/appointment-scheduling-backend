package org.rocs.asa.repository.device.token;

import org.rocs.asa.domain.device.token.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DeviceTokenRepository extends JpaRepository<DeviceToken,Long> {

    DeviceToken findByFcmTokenAndUser_UserId(String fcmToken, String userId);
    DeviceToken findByUser_UserId (String userId);
    DeviceToken findByUser_UserIdAndDeviceType (String userId,String deviceType);
    List<DeviceToken> findAllByFcmTokenAndUser_UserId(String fcmToken, String userId);
    List<DeviceToken> findAllByUser_UserIdAndDeviceType(String userId, String deviceType);
}
