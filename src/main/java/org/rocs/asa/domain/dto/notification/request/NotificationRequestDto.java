package org.rocs.asa.domain.dto.notification.request;

import lombok.Data;

import java.util.Map;
@Data
public class NotificationRequestDto {
    String targetUserId;
    String fcmToken;
    String title;
    String body;
    Map<String, String> data;

}
