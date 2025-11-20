package org.rocs.asa.service.device.token.Impl;

import org.rocs.asa.domain.device.token.DeviceToken;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.UserIdNotFoundOnDeviceToken;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.notification.NotificationRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.device.token.DeviceTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DeviceTokenServiceImpl implements DeviceTokenService {
    Logger LOGGER = LoggerFactory.getLogger(DeviceTokenServiceImpl.class);


    DeviceTokenRepository deviceTokenRepository;
    NotificationRepository notificationRepository;
    UserRepository userRepository;

    @Autowired
    public DeviceTokenServiceImpl(DeviceTokenRepository deviceTokenRepository, NotificationRepository notificationRepository, UserRepository userRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public DeviceToken registerDeviceToken(DeviceToken deviceToken) {

        User user = deviceToken.getUser();
        if (user == null || user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            LOGGER.error("User information is missing in Device Token");
            throw new UserIdNotFoundOnDeviceToken("User ID is missing in Device Token");
        }

        String userId = user.getUserId().trim();
        if (!userId.matches("\\d{10}")) {
            LOGGER.error("Invalid userId format: {}. Expected 10 digits.", userId);
            throw new IllegalArgumentException("Invalid user ID format");
        }

        User existingUser = userRepository.findByUserId(userId);
        if (existingUser == null) {
            LOGGER.info("User not found with userId: {}, cannot register device token", userId);
            throw new IllegalArgumentException("User session not found. Please login again.");
        }

        isValidFCMToken(deviceToken.getFcmToken());

        try {
            DeviceToken existingToken = deviceTokenRepository
                    .findByFcmTokenAndUser_UserId(deviceToken.getFcmToken(), existingUser.getUserId());

            if (existingToken != null) {
                LOGGER.info("Device token already exists for user: {}, updating timestamp", existingUser.getUserId());
                existingToken.setUpdatedAt(LocalDateTime.now());
                return deviceTokenRepository.save(existingToken);
            }

            DeviceToken userExistingToken = deviceTokenRepository
                    .findByUser_UserIdAndDeviceType(existingUser.getUserId(), deviceToken.getDeviceType());

            if (userExistingToken != null) {
                LOGGER.info("User has existing token for device type: {}, updating with new FCM token", deviceToken.getDeviceType());
                userExistingToken.setFcmToken(deviceToken.getFcmToken());
                userExistingToken.setUpdatedAt(LocalDateTime.now());
                return deviceTokenRepository.save(userExistingToken);
            }

            deviceToken.setUser(existingUser);
            deviceToken.setCreatedAt(LocalDateTime.now());
            deviceToken.setUpdatedAt(LocalDateTime.now());

            DeviceToken savedToken = deviceTokenRepository.save(deviceToken);
            LOGGER.info("Successfully registered new device token for user: {}", existingUser.getUserId());

            return savedToken;

        } catch (Exception e) {
            LOGGER.error("Error registering device token for user: {}", userId, e);
            throw new RuntimeException("Failed to register device token", e);
        }
    }


    private boolean isValidFCMToken(String fcmToken) {
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            return false;
        }

        if (fcmToken.length() < 140 || fcmToken.length() > 200) {
            return false;
        }

        return fcmToken.matches("^[a-zA-Z0-9_:-]+$");
    }
}



