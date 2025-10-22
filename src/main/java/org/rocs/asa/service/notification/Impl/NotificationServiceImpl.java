package org.rocs.asa.service.notification.Impl;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
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
    public DeviceToken sendNotificationToUser(String targetUserId, String title, String body, String actionType) {
        DeviceToken token = deviceTokenRepository.findByUser_UserId(targetUserId);

        if (token == null || token.getFcmToken() == null) {
            throw new RuntimeException("No FCM token found for user: " + targetUserId);
        }

        if (token.getDeviceType() == null || token.getDeviceType().isEmpty()) {
            throw new UnknownDeviceTypeException("Unknown Device Type for user: " + targetUserId);
        }

        Map<String, String> data = Map.of("actionType", actionType);

        if (token.getDeviceType().equalsIgnoreCase("WEB") ||
                token.getDeviceType().equalsIgnoreCase("MOBILE")) {
            fcmPushProvider.sendToToken(token.getFcmToken(), title, body, data);
            return token;
        }

        throw new UnknownDeviceTypeException("Unknown Device Type: " + token.getDeviceType());
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
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public boolean markAsRead(Long notificationID) {
        LOGGER.info("Updating Notification marking us read.");
        Notifications notifications = notificationRepository.findById(notificationID)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment Not Found"));

        notifications.setIsRead(1);
        notifications.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notifications);
        return true;
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUser_UserIdAndIsRead(userId, 0);
    }
}
