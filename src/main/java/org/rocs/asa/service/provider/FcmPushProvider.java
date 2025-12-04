package org.rocs.asa.service.provider;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FcmPushProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushProvider.class);

    /**
     * Send notification to a single token
     */
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

    /**
     * Send notification to multiple tokens (batch)
     * Max 500 tokens per batch
     * Returns null if sending fails completely
     */
    public BatchResponse sendToMultipleTokens(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        try {
            if (fcmTokens == null || fcmTokens.isEmpty()) {
                LOGGER.warn("No FCM tokens provided");
                return null;
            }

            List<String> cleanedTokens = new ArrayList<>();
            for (String token : fcmTokens) {
                if (token != null && !token.trim().isEmpty() && !cleanedTokens.contains(token)) {
                    cleanedTokens.add(token);
                }
            }

            if (cleanedTokens.isEmpty()) {
                LOGGER.warn("No valid FCM tokens after cleaning");
                return null;
            }

            LOGGER.info("Attempting to send FCM message to {} tokens", cleanedTokens.size());
            LOGGER.info("Title: {}", title);
            LOGGER.info("Body: {}", body);
            LOGGER.info("Data: {}", data);

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(cleanedTokens)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    );

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            MulticastMessage message = messageBuilder.build();
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            LOGGER.info("Successfully sent {} messages", response.getSuccessCount());
            if (response.getFailureCount() > 0) {
                LOGGER.warn("Failed to send {} messages", response.getFailureCount());

                List<SendResponse> responses = response.getResponses();
                List<String> invalidTokens = new ArrayList<>();

                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String errorCode = String.valueOf(responses.get(i).getException().getErrorCode());
                        String token = cleanedTokens.get(i);

                        LOGGER.error("Failed to send to token {}: {} (Error: {})",
                                token.substring(0, Math.min(50, token.length())),
                                responses.get(i).getException().getMessage(),
                                errorCode);

                        if ("NOT_FOUND".equals(errorCode) ||
                                "UNREGISTERED".equals(errorCode) ||
                                "INVALID_ARGUMENT".equals(errorCode)) {
                            invalidTokens.add(token);
                        }
                    }
                }
                if (!invalidTokens.isEmpty()) {
                    LOGGER.warn("Found {} invalid tokens that should be removed from database", invalidTokens.size());
                }
            }

            return response;
        } catch (FirebaseMessagingException e) {
            LOGGER.error("Firebase Messaging Exception:");
            LOGGER.error("  Error Code: {}", e.getErrorCode());
            LOGGER.error("  Messaging Error Code: {}", e.getMessagingErrorCode());
            LOGGER.error("  Error Message: {}", e.getMessage());
            LOGGER.error("  Full Exception: ", e);
            LOGGER.warn("Returning null due to Firebase exception - operation will continue");
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to send FCM messages - Exception type: {}", e.getClass().getSimpleName());
            LOGGER.error("Failed to send FCM messages - Error message: {}", e.getMessage());
            LOGGER.error("Failed to send FCM messages - Full stack trace: ", e);
            LOGGER.warn("Returning null due to exception - operation will continue");
            return null;
        }
    }

    /**
     * Send notification to multiple tokens in batches (handles more than 500 tokens)
     */
    public void sendToMultipleTokensInBatches(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            LOGGER.warn("No FCM tokens provided");
            return;
        }

        final int BATCH_SIZE = 500;
        int totalTokens = fcmTokens.size();
        int totalSuccess = 0;
        int totalFailure = 0;

        LOGGER.info("Sending notifications to {} tokens in batches of {}", totalTokens, BATCH_SIZE);

        for (int i = 0; i < totalTokens; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, totalTokens);
            List<String> batch = fcmTokens.subList(i, end);

            LOGGER.info("Processing batch {}/{}", (i / BATCH_SIZE) + 1, (totalTokens + BATCH_SIZE - 1) / BATCH_SIZE);

            try {
                BatchResponse response = sendToMultipleTokens(batch, title, body, data);
                if (response != null) {
                    totalSuccess += response.getSuccessCount();
                    totalFailure += response.getFailureCount();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to send batch starting at index {}: {}", i, e.getMessage());
                totalFailure += batch.size();
            }
        }

        LOGGER.info("Batch sending complete. Success: {}, Failure: {}, Total: {}",
                totalSuccess, totalFailure, totalTokens);
    }
}