package org.rocs.asa.repository.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment repository for getting and checking appointment records.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Checks if a student has an appointment with any of the given statuses.
     *
     * @param studentId the student's ID
     * @param status list of statuses to check (ex. PENDING, APPROVED)
     */
    boolean existsByStudentIdAndStatusIn(Long studentId, List<String> status);

    List<Appointment> findByStudent_Id(Long studentId);

    /**
     * Gets all appointments handled by a guidance staff.
     *
     * @param guidanceStaffId the staff ID
     */
    List<Appointment> findByGuidanceStaff_Id(Long guidanceStaffId);

    /**
     * Gets appointments of a guidance staff with a specific status.
     *
     * @param guidanceStaffId the staff ID
     * @param status the appointment status
     */
    List<Appointment> findByGuidanceStaff_IdAndStatusIgnoreCase(Long guidanceStaffId, String status);

    /**
     * Gets appointments by status with loaded student and staff info.
     *
     * @param statuses list of statuses to filter
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.student s " +
            "LEFT JOIN FETCH a.guidanceStaff g " +
            "WHERE a.status IN :statuses")
    List<Appointment> findByStatusInOptimized(@Param("statuses") List<String> statuses);

    /**
     * Gets staff appointments scheduled within a date range.
     *
     * @param id staff ID
     * @param scheduledDate start date and time
     * @param endDate end date and time
     */
    List<Appointment> findByGuidanceStaff_IdAndScheduledDateBetween(
            Long id, LocalDateTime scheduledDate, LocalDateTime endDate);
    /**
     * Checks if a student has an appointment within a date range.
     *
     * @param studentId the student's ID
     * @param statuses statuses to check
     * @param startOfDay start of the time range
     * @param endOfDay end of the time range
     */
    boolean existsByStudentIdAndStatusInAndScheduledDateBetween(
            Long studentId, List<String> statuses, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Gets staff appointments by status within a date range.
     *
     * @param guidanceStaffId the staff ID
     * @param statuses statuses to filter
     * @param startOfDay start of the time range
     * @param endOfDay end of the time range
     */
    List<Appointment> findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
            Long guidanceStaffId, List<String> statuses, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Gets student appointments with a specific status.
     *
     * @param studentId the student's ID
     * @param status appointment status
     */
    List<Appointment> findByStudent_IdAndStatusIgnoreCase(Long studentId, String status);
}
