package org.rocs.asa.domain.dto.appointment.request;

import lombok.Data;
import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.user.User;

@Data
public class AppointmentRequestDto {

    Appointment appointment;
    User user;
    String message;
    String actionType;
}
