package org.rocs.asa.controller.notification;

import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.dto.device.token.request.DeviceTokenRequest;
import org.rocs.asa.exception.domain.EmptyFieldException;
import org.rocs.asa.exception.domain.FcmTokenNotFoundException;
import org.rocs.asa.exception.domain.UserIdNotFoundOnDeviceToken;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.service.device.token.DeviceTokenService;
import org.rocs.asa.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/notification")
@CrossOrigin("*")
public class NotificationController {
   private static Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);


    private DeviceTokenService deviceTokenService;
    private NotificationService notificationService;

    @Autowired
    public NotificationController(DeviceTokenService deviceTokenService, NotificationService notificationService) {
        this.deviceTokenService = deviceTokenService;
        this.notificationService = notificationService;
    }

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

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notifications>> getAllNotification (@PathVariable String userId){
        List<Notifications> retrieveNotification =  notificationService.getNotificationByUser(userId);
        return ResponseEntity.ok(retrieveNotification);
    }

    @PatchMapping("/markAsRead/{appointmentId}")
    public ResponseEntity<String> markAsRead (@PathVariable Long appointmentId){
        boolean markAsRead = notificationService.markAsRead(appointmentId);
        return ResponseEntity.ok(" Successfully updated as read");

    }
    @GetMapping("/unreadCount/{userId}")
    public ResponseEntity<Long> countUnread (@PathVariable String userId){
        Long unread = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(unread);
    }
}
