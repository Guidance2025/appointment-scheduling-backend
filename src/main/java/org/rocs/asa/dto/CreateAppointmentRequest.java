package org.rocs.asa.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {
    private Long studentId;
    private Long guidanceStaffId;
    private LocalDateTime scheduledDate;
    private LocalDateTime endDate;
    private String appointmentType; // "COUNSELING", "ACADEMIC", "CAREER", etc.
    private String notes;
}
