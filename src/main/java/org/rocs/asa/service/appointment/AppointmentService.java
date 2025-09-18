package org.rocs.asa.service.appointment;

import org.rocs.asa.domain.appointment.Appointment;

import java.util.List;

/**
 * {@code AppointmentService} is an interface of the Appointment Service
 * */
public interface AppointmentService {

    Appointment createAppointment(Appointment appointment);

    List<Appointment> getAllAppointments();

    Appointment findAppointmentsByAppointmentId(Long appointmentId);

    List<Appointment> findAppointmentByStatus(String status);

}
