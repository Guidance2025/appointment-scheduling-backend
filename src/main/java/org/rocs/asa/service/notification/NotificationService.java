package org.rocs.asa.service.notification;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;

import java.util.List;

/**
 * Service interface for managing notifications.
 * Implementations can handle sending notifications to users
 * and saving notifications to the database.
 */
public interface NotificationService {

    /**
     * Sends a notification to a user device using the available push mechanism.
     *
     * @param targetUserId the target user's ID
     * @param title        the notification title
     * @param body         the notification body/message
     * @param actionType   the type of action for this notification
     * @return the device token used for sending
     */
    List <DeviceToken> sendNotificationToUser(String targetUserId, String title, String body , String actionType);

    /**
     * Saves a notification record for a user and appointment.
     *
     * @param user        the user to associate with the notification
     * @param appointment the appointment associated with the notification
     * @param message     the notification message
     * @param actionType  the type of action for this notification
     * @return the saved notification entity
     */
    Notifications saveNotification(User user, Appointment appointment, String message, String actionType);


    List<Notifications> getNotificationByUser(String userId);

    boolean markAsRead(Long notificationID);

    long getUnreadCount(String userId);


}
