package org.rocs.asa.dto.appointment.create.appointment.response;

import lombok.Data;
import org.rocs.asa.domain.appointment.Appointment;

import java.time.LocalDateTime;

@Data
public class AppointmentResponseDto {
    private Long appointmentId;
    private String studentName;
    private String employeeName;
    private LocalDateTime scheduledDate;
    private String appointmentType;
    private String status;
    private String notes;


public AppointmentResponseDto(Appointment appointment) {
        this.appointmentId = appointment.getAppointmentId();
        this.studentName = appointment.getStudent().getPerson().getFirstName()
                + " " + appointment.getStudent().getPerson().getLastName();
        this.employeeName = appointment.getGuidanceStaff().getPerson().getFirstName()
                + " " + appointment.getGuidanceStaff().getPerson().getLastName();
        this.scheduledDate = appointment.getScheduledDate();
        this.appointmentType = appointment.getAppointmentType();
        this.status = appointment.getStatus();
        this.notes = appointment.getNotes();
    }
}

