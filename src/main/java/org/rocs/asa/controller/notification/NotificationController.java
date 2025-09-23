package org.rocs.asa.controller.notification;

import com.google.api.Http;
import oracle.jdbc.proxy.annotation.Post;
import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.dto.notification.request.NotificationRequest;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.dto.device.token.request.DeviceTokenRequest;
import org.rocs.asa.exception.domain.UserIdNotFoundOnDeviceToken;
import org.rocs.asa.service.device.token.DeviceTokenService;
import org.rocs.asa.service.notication.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
        try {
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User ID is required"));
            }

            if (request.getFcmToken() == null || request.getFcmToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "FCM token is required"));
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

        } catch (UserIdNotFoundOnDeviceToken e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            LOGGER.error("Failed to register device token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register device token"));
        }
    }


}
