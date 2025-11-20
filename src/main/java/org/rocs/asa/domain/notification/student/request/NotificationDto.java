package org.rocs.asa.domain.notification.student.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
   private Long appointmentId;
   private String message;
   private String actionType;
   private LocalDateTime createdAt;
   private Long notificationId;
}
