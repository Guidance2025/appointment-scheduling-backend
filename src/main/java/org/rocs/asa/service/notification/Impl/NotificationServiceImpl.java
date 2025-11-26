package org.rocs.asa.service.notification.Impl;

import org.aspectj.weaver.ast.Not;
import org.checkerframework.checker.units.qual.N;
import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
import org.rocs.asa.exception.domain.DeviceTokenAlreadyExist;
import org.rocs.asa.exception.domain.UnknownDeviceTypeException;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.provider.FcmPushProvider;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.notification.NotificationRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private UserRepository userRepository;
    private DeviceTokenRepository deviceTokenRepository;
    private FcmPushProvider fcmPushProvider;
    private NotificationRepository notificationRepository;
    private AppointmentRepository appointmentRepository;

    @Autowired
    public NotificationServiceImpl(UserRepository userRepository,
                                   DeviceTokenRepository deviceTokenRepository,
                                   FcmPushProvider fcmPushProvider,
                                   NotificationRepository notificationRepository,
                                   AppointmentRepository appointmentRepository
    ){
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.fcmPushProvider = fcmPushProvider;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
    }
    @Override
    public List<DeviceToken> sendNotificationToUser(String targetUserId, String title, String body, String actionType) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUser_UserId(targetUserId);
        if (tokens.isEmpty()) {
            LOGGER.warn("No device tokens found for user: {}", targetUserId);
            throw new DeviceTokenAlreadyExist("Device Token does not exist ");
        }

        Map<String, String> data = Map.of("actionType", actionType);
        List<DeviceToken> sentTokens = new ArrayList<>();

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
            } catch (Exception e) {
                LOGGER.error("Failed to send notification to device {}: {}", token.getDeviceType(), e.getMessage());
            }
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
    public List<Notifications> getNotificationByUser(String userId) {
        if(userId == null ) {
            LOGGER.info("User Not Found");
            throw new UserNotFoundException("User not Found");
        }
        return notificationRepository.findNotificationsByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(String userId) {
        LOGGER.info("Updating Notification marking us read.");
        List<Notifications> notificationsList = notificationRepository.findNotificationsByUser_UserId(userId);
        for(Notifications updateNotification : notificationsList ){
            updateNotification.setIsRead(1);
            updateNotification.setUpdatedAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(notificationsList);
        LOGGER.info("Mark as Read Successfully");
    }

    @Override
    public List<Notifications> markAsReadMobile(Long notificationId) {
        List<Notifications> notificationsList = notificationRepository.findByNotificationId(notificationId);

        for(Notifications notifications : notificationsList) {
            notifications.setIsRead(1);
            notifications.setUpdatedAt(LocalDateTime.now());
        }
         LOGGER.info("Mobile Mark as Read Successfully");
         return notificationRepository.saveAll(notificationsList);
    }
    @Override
    public long getUnreadCount(String userId) {
         return notificationRepository.countByUser_UserIdAndIsRead(userId, 0);
    }

    @Override
    public void clearNotification(String userId) {
        List<Notifications> activeNotification = notificationRepository.findActiveNotificationByUserId(userId);
        for(Notifications updateNotification : activeNotification) {
            updateNotification.setStatus("INACTIVE");
        }
        notificationRepository.saveAll(activeNotification);
        LOGGER.info("Cleared Successfully ");
    }
}
