package org.rocs.asa.controller.notification;

import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.domain.device.token.request.DeviceTokenRequest;
import org.rocs.asa.exception.domain.FcmTokenNotFoundException;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.service.device.token.DeviceTokenService;
import org.rocs.asa.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code NotificationController} handles all notification and device token operations
 * @author ROCS
 * @version 1.0
 */
@RestController
@RequestMapping("/notification")
@CrossOrigin("*")
public class NotificationController {
    private static Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    private DeviceTokenService deviceTokenService;
    private NotificationService notificationService;

    /**
     * Constructs a new {@code NotificationController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     * @param deviceTokenService the service layer for managing device token operations
     * @param notificationService the service layer for managing notification operations
     */
    @Autowired
    public NotificationController(DeviceTokenService deviceTokenService, NotificationService notificationService) {
        this.deviceTokenService = deviceTokenService;
        this.notificationService = notificationService;
    }

    /**
     * {@code registerDeviceToken} used to register a device FCM token for push notifications
     * @param request that contains the user ID, FCM token, and device type
     * @return ResponseEntity containing success status and registration details, and Http Status
     * @throws UserNotFoundException if user ID is null or empty
     * @throws FcmTokenNotFoundException if FCM token is null or empty
     */
    @PostMapping("/register-token")
    public ResponseEntity<?> registerDeviceToken(@RequestBody DeviceTokenRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new UserNotFoundException("User ID not found");
        }
        if (request.getFcmToken() == null || request.getFcmToken().trim().isEmpty()) {
            throw new FcmTokenNotFoundException("Fcm Token not Found");
        }

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setFcmToken(request.getFcmToken());
        deviceToken.setDeviceType(request.getDeviceType());

        User user = new User();
        user.setUserId(request.getUserId());
        deviceToken.setUser(user);

        DeviceToken savedToken = deviceTokenService.registerDeviceToken(deviceToken);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Device token registered successfully");
        response.put("userId", savedToken.getUser().getUserId());

        return ResponseEntity.ok(response);
    }

    /**
     * {@code getAllNotification} used to retrieve all notifications for a specific user
     * @param userId that identifies the user
     * @return ResponseEntity containing list of user notifications, and Http Status
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Notifications>> getAllNotification(@PathVariable String userId) {
        List<Notifications> retrieveNotification = notificationService.getNotificationByUser(userId);
        return ResponseEntity.ok(retrieveNotification);
    }

    /**
     * {@code markAsRead} used to mark a notification as read
     * @param notificationId that identifies the notification
     * @return ResponseEntity containing success message, and Http Status
     */
    @PatchMapping("/markAsRead/{notificationId}")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        boolean markAsRead = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Successfully updated as read");
    }

    /**
     * {@code countUnread} used to retrieve the count of unread notifications for a user
     * @param userId that identifies the user
     * @return ResponseEntity containing the unread notification count, and Http Status
     */
    @GetMapping("/unreadCount/{userId}")
    public ResponseEntity<Long> countUnread(@PathVariable String userId) {
        Long unread = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(unread);
    }
}