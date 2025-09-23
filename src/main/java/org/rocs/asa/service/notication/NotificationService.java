package org.rocs.asa.service.notication;

import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.user.User;

import java.util.List;
import java.util.Map;

public interface NotificationService {


    boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data);

    boolean sendNotificationMultipleDevice(List<String> fcmTokens, String title, String body, Map<String, String> data);

    boolean sendNotificationToTopic(String topic, String title, String body, Map<String, String> data);
}
