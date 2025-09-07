package org.rocs.asa.dto.appointment.create.appointment.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CreateAppointmentRequestDto {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Employee Number is required")
    private Long employeeNumber;
    @NotNull(message = "Scheduled date is required")

    @Future(message = "Scheduled date must be in the future")
    private LocalDateTime scheduledDate;

    @NotBlank(message = "Appointment type is required")
    private String appointmentType;

    private String status;

    private String notes;
}
