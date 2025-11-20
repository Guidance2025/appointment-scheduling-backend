package org.rocs.asa.domain.appointment.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {
    private Long studentId;
    private Long guidanceStaffId;
    private LocalDateTime scheduledDate;
    private LocalDateTime endDate;
    private String appointmentType;
    private String notes;
}
