package org.rocs.asa.service.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.response.BookedSlotsResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing appointments between students and guidance staff.
 * Provides operations for creating, updating, and querying appointments.
 */
public interface AppointmentService {

    /**
     * Creates an appointment for a student by the authenticated guidance staff.
     *
     * @param request the appointment details to create
     * @return the created appointment
     */
    Appointment createAppointment(Appointment request);

    /**
     * Creates an appointment initiated by the authenticated student.
     *
     * @param appointment the appointment details including guidance staff selection
     * @return the created appointment
     */
    Appointment studentCreateAppointment(Appointment appointment);

    /**
     * Handles guidance staff response to a pending appointment request.
     *
     * @param appointmentId the ID of the appointment to respond to
     * @param data map containing the action ("accept" or "decline")
     * @return the updated appointment
     */
    Appointment guidanceStaffResponse(Long appointmentId, Map<String, String> data);

    /**
     * Handles student response to a pending appointment request.
     *
     * @param appointmentId the ID of the appointment to respond to
     * @param data map containing the action ("accept" or "decline")
     * @return the updated appointment
     */
    Appointment studentResponseToAppointment(Long appointmentId, Map<String, String> data);

    /**
     * Finds an appointment by its ID.
     *
     * @param appointmentId the appointment ID
     * @return the appointment
     */
    Appointment findAppointmentsByAppointmentId(Long appointmentId);

    /**
     * Finds appointments by guidance staff ID and status.
     *
     * @param guidanceStaffId the ID of the guidance staff
     * @param status the appointment status to filter by
     * @return list of appointments matching the criteria
     */
    List<Appointment> findAppointmentByStatus(Long guidanceStaffId, String status);

    /**
     * Finds student appointments filtered by status.
     *
     * @param studentId the student ID
     * @param status the appointment status
     * @return list of appointments matching the criteria
     */
    List<Appointment> findStudentAppointments(Long studentId, String status);

    /**
     * Gets all appointments for a specific guidance staff member.
     *
     * @param employeeNumber the guidance staff employee number
     * @return list of appointments
     */
    List<Appointment> getAppointmentByGuidanceStaff(Long employeeNumber);

    /**
     * Gets all appointments for a specific student.
     *
     * @param id the student ID
     * @return list of appointments
     */
    List<Appointment> getAppointmentByStudent(Long id);

    /**
     * Gets booked time slots for the authenticated guidance staff on a specific date.
     *
     * @param date the date in ISO format (yyyy-MM-dd)
     * @return list of booked slots with start and end times
     */
    List<BookedSlotsResponse> getBookedSlots(String date);

    /**
     * Updates appointment statuses to ONGOING or COMPLETED based on current time.
     * This method should be called by a scheduled job.
     */
    void markAsOnGoingOrIsCompleted();

    void sendAppointmentReminders();
}