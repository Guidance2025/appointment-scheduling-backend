package org.rocs.asa.domain.appointment.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RescheduleAppointmentRequest {
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "New scheduled date is required")
    private LocalDateTime newScheduledDate;

    @NotNull(message = "New end date is required")
    private LocalDateTime newEndDate;

    private String reason;
}
