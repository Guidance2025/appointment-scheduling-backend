package org.rocs.asa.service.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.response.BookedSlotsResponse;

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

    List<BookedSlotsResponse> getBookedSlots(String date);

    List<Appointment> findStudentAppointments(Long studentId, String status);

    Appointment studentCreateAppointment(Appointment appointment);
    Appointment guidanceStaffResponse(Long appointmentId,Map<String,String> data);
}
