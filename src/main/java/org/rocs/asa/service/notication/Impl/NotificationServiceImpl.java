package org.rocs.asa.service.notication.Impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Message.Builder;
import com.google.firebase.messaging.Notification;
import org.rocs.asa.service.notication.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        LOGGER.info("Sending Notification '{}' to device", title);

        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            LOGGER.error("FCM token is null or empty");
            return false;
        }

        if (title == null || title.trim().isEmpty()) {
            LOGGER.error("Notification title is required");
            return false;
        }

        if (body == null || body.trim().isEmpty()) {
            LOGGER.error("Notification body is required");
            return false;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());


            if (data != null || !data.isEmpty()) {
                messageBuilder.putAllData(data);
                LOGGER.debug("Added {} custom data fields", data.size());
            }


            Message message = Message.builder().build();
            String messageId = FirebaseMessaging.getInstance().send(message);

            LOGGER.info("Notification Sent Successfully");

            return true;


        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean sendNotificationMultipleDevice(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        return false;
    }

    @Override
    public boolean sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        return false;
    }
}
