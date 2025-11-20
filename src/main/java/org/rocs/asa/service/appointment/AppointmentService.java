package org.rocs.asa.service.appointment;

import org.rocs.asa.domain.appointment.Appointment;

import java.util.List;
import java.util.Map;

/**
 * {@code AppointmentService} is an interface of the Appointment Service
 * */
public interface AppointmentService {

    Appointment createAppointment(Appointment request);

    List<Appointment> getAllAppointments();

    Appointment findAppointmentsByAppointmentId(Long appointmentId);

    List<Appointment> findAppointmentByStatus( Long guidanceStaffId,String status);

    Appointment studentResponseToAppointment(Long appointmentId, Map<String,String> data);

    List<Appointment> getAppointmentByGuidanceStaff (Long employeeNumber);

    void markAsOnGoingOrIsCompleted();
}
