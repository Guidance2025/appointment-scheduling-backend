package org.rocs.asa.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RescheduleRequest {
    private LocalDateTime newScheduledDate;
    private LocalDateTime newEndDate;
    private String reason;
    private Long studentId; // Student to notify
}
