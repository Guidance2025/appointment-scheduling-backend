package org.rocs.asa.service.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.request.RescheduleAppointmentRequest;
import org.rocs.asa.domain.appointment.request.UpdateAppointmentRequest;
import org.rocs.asa.domain.appointment.response.BookedSlotsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing appointments between students and guidance staff.
 * Provides operations for creating, updating, querying, and managing availability.
 */
public interface AppointmentService {

    /**
     * Creates an appointment on behalf of a student by the authenticated guidance staff.
     *
     * @param request the appointment details
     * @return the created appointment
     */
    Appointment createAppointment(Appointment request);

    /**
     * Creates an appointment initiated by a student.
     *
     * @param appointment the appointment details including guidance staff selection
     * @return the created appointment
     */
    Appointment studentCreateAppointment(Appointment appointment);

    /**
     * Processes a guidance staff response to a pending appointment request.
     *
     * @param appointmentId the ID of the appointment
     * @param data          map containing action ("accept" or "decline")
     * @return the updated appointment
     */
    Appointment guidanceStaffResponse(Long appointmentId, Map<String, String> data);

    /**
     * Processes a guidance staff response to a reschedule request.
     *
     * @param appointmentId the ID of the appointment
     * @param data          map containing action ("accept" or "decline")
     * @return the updated appointment
     */
    Appointment rescheduleAppointmentResponse(Long appointmentId, Map<String, String> data);

    /**
     * Processes a student response to a pending appointment request.
     *
     * @param appointmentId the ID of the appointment
     * @param data          map containing action ("accept" or "decline")
     * @return the updated appointment
     */
    Appointment studentResponseToAppointment(Long appointmentId, Map<String, String> data);

    /**
     * Finds an appointment by its ID.
     *
     * @param appointmentId the appointment ID
     * @return the appointment, or null if not found
     */
    Appointment findAppointmentsByAppointmentId(Long appointmentId);

    /**
     * Retrieves appointments for a guidance staff member filtered by status.
     *
     * @param guidanceStaffId the staff member ID
     * @return list of appointments matching the status
     */
    List<Appointment> findAppointmentByStatus(Long guidanceStaffId);

    /**
     * Retrieves appointments for a student filtered by status.
     *
     * @param studentId the student ID
     * @param status    list of appointment statuses to filter
     * @return list of matching appointments
     */
    List<Appointment> findStudentAppointments(Long studentId, List<String> status);

    /**
     * Retrieves all appointments for a specific guidance staff member.
     *
     * @param employeeNumber the staff member's employee number
     * @return list of appointments
     */
    List<Appointment> getAppointmentByGuidanceStaff(Long employeeNumber);

    /**
     * Retrieves all appointments for a specific student.
     *
     * @param id the student ID
     * @return list of appointments
     */
    List<Appointment> getAppointmentByStudent(Long id);

    /**
     * Retrieves booked time slots for a guidance staff member on a specific date.
     *
     * @param date the date in ISO format (yyyy-MM-dd)
     * @return list of booked slots with start and end times
     */
    List<BookedSlotsResponse> getBookedSlots(String date);

    /**
     * Updates appointment details by a counselor.
     *
     * @param request the appointment update request
     * @return the updated appointment
     */
    Appointment updateCounselorAppointment(UpdateAppointmentRequest request);

    /**
     * Cancels an appointment.
     *
     * @param appointmentId the ID of the appointment
     * @return true if successfully cancelled
     */
    boolean cancelAppointment(Long appointmentId);

    /**
     * Student requests to reschedule an appointment.
     * <ul>
     *     <li>Automatic approval if >48 hours before appointment</li>
     *     <li>Counselor approval required if <48 hours</li>
     * </ul>
     *
     * @param request the reschedule request
     * @return the updated appointment
     */
    Appointment studentRescheduleAppointment(RescheduleAppointmentRequest request);

    /**
     * Counselor responds to a reschedule request.
     *
     * @param appointmentId the appointment ID
     * @param data          map containing action ("accept" or "decline")
     * @return the updated appointment
     */
    Appointment counselorResponseToReschedule(Long appointmentId, Map<String, String> data);

    /**
     * Updates appointments to ONGOING or COMPLETED based on current time.
     * Intended to be run by a scheduled job.
     */
    void markAsOnGoingOrIsCompleted();

    /** Sends appointment reminders to students and guidance staff. */
    void sendAppointmentReminders();

    /** Cleans up expired availability blocks. */
    void cleanupExpiredAvailabilityBlocks();

    /** Expires pending appointment requests past their validity. */
    void expirePendingRequests();

    /** Expires pending reschedule requests past their validity. */
    void expireReschedulePendingRequests();

    /**
     * Creates an availability block for a guidance staff member.
     * Marks a time period as unavailable for appointments.
     *
     * @param guidanceStaffId ID of the staff member
     * @param scheduledDate   start of blocked period
     * @param endDate         end of blocked period (null = full day)
     * @param reason          optional reason for the block
     * @return the created availability block
     */
    Appointment createAvailabilityBlock(Long guidanceStaffId,
                                        LocalDateTime scheduledDate,
                                        LocalDateTime endDate,
                                        String reason);

    /** Retrieves all availability blocks for a guidance staff member. */
    List<Appointment> getAvailabilityBlocks(Long guidanceStaffId);

    /** Retrieves availability blocks within a date range. */
    List<Appointment> getAvailabilityBlocksInRange(Long guidanceStaffId,
                                                   LocalDateTime startDate,
                                                   LocalDateTime endDate);

    /** Updates an existing availability block. */
    Appointment updateAvailabilityBlock(Long blockId,
                                        LocalDateTime scheduledDate,
                                        LocalDateTime endDate,
                                        String reason);

    /** Deletes an existing availability block. */
    boolean deleteAvailabilityBlock(Long blockId);

    /** Creates a month-long leave block (Mon-Fri) for a guidance staff member. */
    List<Appointment> createMonthLeave(Long guidanceStaffId, int year, int month, String reason);

    /** Retrieves all leave blocks (month/week) for a guidance staff member. */
    List<Appointment> getLeaveBlocks(Long guidanceStaffId);

    /** Deletes all month-long leave blocks for a guidance staff member. */
    int deleteMonthLeave(Long guidanceStaffId, int year, int month);
}
