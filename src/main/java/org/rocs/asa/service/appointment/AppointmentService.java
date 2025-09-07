package org.rocs.asa.service.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.dto.appointment.create.appointment.request.CreateAppointmentRequestDto;
import org.rocs.asa.dto.appointment.create.appointment.response.AppointmentResponseDto;

import java.util.List;
import java.util.Optional;

/**
 * {@code AppointmentService} is an interface of the Appointment Service
 * */
public interface AppointmentService {

    AppointmentResponseDto createAppointment(CreateAppointmentRequestDto dto);

    List<Appointment> getAllAppointments();

    Appointment findAppointmentsByAppointmentId(Long appointmentId);

    List<Appointment> findAppointmentByStatus(String status);

}
