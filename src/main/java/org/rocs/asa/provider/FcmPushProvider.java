package org.rocs.asa.provider;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FcmPushProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushProvider.class);

        public void sendToToken(String fcmToken, String title, String body, Map<String, String> data) {
            try {
                LOGGER.info("Attempting to send FCM message...");
                LOGGER.info("Token (first 50 chars): {}", fcmToken.substring(0, Math.min(50, fcmToken.length())));
                LOGGER.info("Token length: {}", fcmToken.length());
                LOGGER.info("Title: {}", title);
                LOGGER.info("Body: {}", body);
                LOGGER.info("Data: {}", data);

                Message.Builder messageBuilder = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(
                                Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build()
                        );

                if (data != null && !data.isEmpty()) {
                    messageBuilder.putAllData(data);
                }

                Message message = messageBuilder.build();
                String response = FirebaseMessaging.getInstance().send(message);
                LOGGER.info("Successfully Sent Message: {}", response);

            } catch (FirebaseMessagingException e) {
                LOGGER.error("Firebase Messaging Exception:");
                LOGGER.error("  Error Code: {}", e.getErrorCode());
                LOGGER.error("  Messaging Error Code: {}", e.getMessagingErrorCode());
                LOGGER.error("  Error Message: {}", e.getMessage());
                LOGGER.error("  Full Exception: ", e);

                if (e.getMessage() != null && e.getMessage().contains("SenderId mismatch")) {
                    LOGGER.error("SENDER_ID_MISMATCH DETECTED!");
                    LOGGER.error("This FCM token was generated for a different Firebase project");
                }

                throw new RuntimeException("FCM Error: " + e.getMessage(), e);

            } catch (Exception e) {
                LOGGER.error("Failed to send FCM message - Exception type: {}", e.getClass().getSimpleName());
                LOGGER.error("Failed to send FCM message - Error message: {}", e.getMessage());
                LOGGER.error("Failed to send FCM message - Full stack trace: ", e);
                throw new RuntimeException("Error sending FCM message", e);
            }
        }
    }
