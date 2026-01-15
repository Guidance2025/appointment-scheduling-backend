package org.rocs.asa.domain.appointment.request;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UpdateAppointmentRequest {
    private Long appointmentId;
    private LocalDateTime scheduledDate;
    private LocalDateTime endDate;
}
