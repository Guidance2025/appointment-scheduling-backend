package org.rocs.asa.service.notification.Impl;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.*;
import org.rocs.asa.service.provider.FcmPushProvider;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.notification.NotificationRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.student.StudentService;
import org.rocs.asa.utils.security.enumeration.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmPushProvider fcmPushProvider;
    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;
    private final StudentService studentService;

    @Autowired
    public NotificationServiceImpl(UserRepository userRepository,
                                   DeviceTokenRepository deviceTokenRepository,
                                   FcmPushProvider fcmPushProvider,
                                   NotificationRepository notificationRepository,
                                   AppointmentRepository appointmentRepository,
                                   StudentService studentService) {
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.fcmPushProvider = fcmPushProvider;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.studentService = studentService;
    }

    @Override
    public List<DeviceToken> sendNotificationToUser(String targetUserId, String title, String body, String actionType) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUser_UserId(targetUserId);

        if (tokens.isEmpty()) {
            LOGGER.warn("No device tokens found for user: {}", targetUserId);
            throw new DeviceTokenAlreadyExist("Device Token does not exist");
        }

        Map<String, String> data = Map.of("actionType", actionType);
        List<DeviceToken> sentTokens = new ArrayList<>();
        List<String> invalidTokens = new ArrayList<>();

        for (DeviceToken token : tokens) {
            try {
                if (token.getDeviceType().equalsIgnoreCase("WEB") ||
                        token.getDeviceType().equalsIgnoreCase("MOBILE")) {
                    fcmPushProvider.sendToToken(token.getFcmToken(), title, body, data);
                    sentTokens.add(token);
                    LOGGER.info("Notification sent to {} device for user: {}", token.getDeviceType(), targetUserId);
                } else {
                    LOGGER.warn("Unknown Device Type: {} for user: {}", token.getDeviceType(), targetUserId);
                }
            } catch (RuntimeException e) {
                LOGGER.error("Failed to send notification to device {}: {}", token.getDeviceType(), e.getMessage());

                if (e.getMessage() != null &&
                        (e.getMessage().contains("NOT_FOUND") ||
                                e.getMessage().contains("UNREGISTERED") ||
                                e.getMessage().contains("INVALID_ARGUMENT"))) {
                    invalidTokens.add(token.getFcmToken());
                }
            }
        }

        // Clean up invalid tokens
        if (!invalidTokens.isEmpty()) {
            removeInvalidTokens(invalidTokens);
        }

        if (sentTokens.isEmpty()) {
            throw new UnknownDeviceTypeException("No valid device types found for user: " + targetUserId);
        }

        return sentTokens;
    }

    @Override
    public Notifications saveNotification(User user, Appointment appointment, String message, String actionType) {
        appointmentRepository.findById(appointment.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment Not Found with id " + appointment.getAppointmentId()));

        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setAppointment(appointment);
        notification.setMessage(message);
        notification.setActionType(actionType);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public List<Notifications> sendNotificationToAllStudent(
            List<String> targetUserIds,
            String title,
            String body,
            String actionType) {

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new EmptyFieldException("User ID list cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new EmptyFieldException("Title cannot be empty");
        }
        if (body == null || body.isBlank()) {
            throw new EmptyFieldException("Body cannot be empty");
        }
        if (actionType == null || actionType.isBlank()) {
            throw new EmptyFieldException("ActionType cannot be empty");
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUser_UserIdIn(targetUserIds);

        List<String> fcmTokens = new ArrayList<>();
        Map<String, User> uniqueStudentUsers = new HashMap<>();

        for (DeviceToken token : deviceTokens) {
            User user = token.getUser();

            if (Role.STUDENT_ROLE.name().equals(user.getRole())) {
                fcmTokens.add(token.getFcmToken());
                uniqueStudentUsers.putIfAbsent(user.getUserId(), user);
            }
        }

        LOGGER.info("Found {} student users with {} FCM tokens", uniqueStudentUsers.size(), fcmTokens.size());

        if (!fcmTokens.isEmpty()) {
            try {
                Map<String, String> data = Map.of("actionType", actionType);
                BatchResponse response = fcmPushProvider.sendToMultipleTokens(fcmTokens, title, body, data);

                if (response != null) {
                    LOGGER.info("FCM Batch Result: {} successful, {} failed out of {} tokens",
                            response.getSuccessCount(), response.getFailureCount(), fcmTokens.size());

                    List<String> invalidTokens = extractInvalidTokens(response, fcmTokens);
                    if (!invalidTokens.isEmpty()) {
                        removeInvalidTokens(invalidTokens);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to send FCM notifications: {}", e.getMessage());
                LOGGER.warn("Continuing to save notification records despite FCM failure");
            }
        } else {
            LOGGER.warn("No FCM tokens available to send notifications");
        }

        // --- 4. SAVE NOTIFICATION RECORDS (ONE PER USER) ---
        List<Notifications> savedNotifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (User student : uniqueStudentUsers.values()) {
            Notifications notif = new Notifications();
            notif.setUser(student);
            notif.setMessage(body);
            notif.setActionType(actionType);
            notif.setIsRead(0);
            notif.setCreatedAt(now);
            notif.setUpdatedAt(now);

            savedNotifications.add(notificationRepository.save(notif));
        }

        LOGGER.info("Saved {} notification records to database", savedNotifications.size());

        return savedNotifications;
    }

    @Override
    public List<Notifications> getNotificationByUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.error("User ID is null or empty");
            throw new UserNotFoundException("User not Found");
        }
        return notificationRepository.findNotificationsByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String userId) {
        LOGGER.info("Marking all notifications as read for user: {}", userId);

        List<Notifications> notificationsList = notificationRepository.findNotificationsByUser_UserId(userId);

        if (notificationsList.isEmpty()) {
            LOGGER.info("No unread notifications found for user: {}", userId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Notifications notification : notificationsList) {
            notification.setIsRead(1);
            notification.setUpdatedAt(now);
        }

        notificationRepository.saveAll(notificationsList);
        LOGGER.info("Successfully marked {} notifications as read for user: {}", notificationsList.size(), userId);
    }

    @Override
    @Transactional
    public List<Notifications> markAsReadMobile(Long notificationId) {
        List<Notifications> notificationsList = notificationRepository.findByNotificationId(notificationId);

        if (notificationsList.isEmpty()) {
            LOGGER.warn("No notifications found with id: {}", notificationId);
            return notificationsList;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Notifications notification : notificationsList) {
            notification.setIsRead(1);
            notification.setUpdatedAt(now);
        }

        LOGGER.info("Mobile: Marked {} notification(s) as read", notificationsList.size());
        return notificationRepository.saveAll(notificationsList);
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUser_UserIdAndIsRead(userId, 0);
    }

    @Override
    @Transactional
    public void clearNotification(String userId) {
        List<Notifications> activeNotifications = notificationRepository.findActiveNotificationByUserId(userId);

        if (activeNotifications.isEmpty()) {
            LOGGER.info("No active notifications to clear for user: {}", userId);
            return;
        }

        for (Notifications notification : activeNotifications) {
            notification.setStatus("INACTIVE");
        }

        notificationRepository.saveAll(activeNotifications);
        LOGGER.info("Cleared {} notifications for user: {}", activeNotifications.size(), userId);
    }


    /**
     * Extract invalid tokens from batch response
     */
    private List<String> extractInvalidTokens(BatchResponse response, List<String> sentTokens) {
        List<String> invalidTokens = new ArrayList<>();

        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();

            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    String errorCode = String.valueOf(responses.get(i).getException().getErrorCode());

                    // Identify tokens that should be removed
                    if ("NOT_FOUND".equals(errorCode) ||
                            "UNREGISTERED".equals(errorCode) ||
                            "INVALID_ARGUMENT".equals(errorCode)) {

                        String invalidToken = sentTokens.get(i);
                        invalidTokens.add(invalidToken);

                        LOGGER.warn("Invalid token detected ({}): {}...",
                                errorCode,
                                invalidToken.substring(0, Math.min(50, invalidToken.length())));
                    }
                }
            }
        }

        return invalidTokens;
    }

    /**
     * Remove invalid FCM tokens from database
     */
    @Transactional
    private void removeInvalidTokens(List<String> invalidTokens) {
        if (invalidTokens == null || invalidTokens.isEmpty()) {
            return;
        }

        try {
            LOGGER.info("Removing {} invalid FCM tokens from database", invalidTokens.size());

            for (String token : invalidTokens) {
                List<DeviceToken> tokensToDelete = deviceTokenRepository.findByFcmToken(token);
                if (!tokensToDelete.isEmpty()) {
                    deviceTokenRepository.deleteAll(tokensToDelete);
                    LOGGER.info("Deleted invalid token: {}...", token.substring(0, Math.min(50, token.length())));
                }
            }

            LOGGER.info("Successfully removed {} invalid tokens", invalidTokens.size());
        } catch (Exception e) {
            LOGGER.error("Failed to remove invalid tokens: {}", e.getMessage());
        }
    }
}