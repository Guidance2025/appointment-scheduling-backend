package org.rocs.asa.repository.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentTypeAndStatusAndEndDateBefore(
            String appointmentType,
            String status,
            LocalDateTime now
    );

    /**
     * Find appointments for student with specific counselor in date range
     * Used for update validation
     */
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.student.id = :studentId " +
            "AND a.guidanceStaff.id = :guidanceStaffId " +
            "AND a.status IN :statuses " +
            "AND a.scheduledDate BETWEEN :start AND :end " +
            "AND a.appointmentType != 'AVAILABILITY_BLOCK'")
    List<Appointment> findByStudent_IdAndGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
            @Param("studentId") Long studentId,
            @Param("guidanceStaffId") Long guidanceStaffId,
            @Param("statuses") List<String> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Check if student has appointment with specific counselor on a specific day
     * Used to prevent same counselor from booking multiple appointments with same student per day
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
            "WHERE a.student.id = :studentId " +
            "AND a.guidanceStaff.id = :guidanceStaffId " +
            "AND a.status IN :statuses " +
            "AND a.scheduledDate BETWEEN :start AND :end " +
            "AND a.appointmentType != 'AVAILABILITY_BLOCK'")
    boolean existsByStudentIdAndGuidanceStaffIdAndStatusInAndScheduledDateBetween(
            @Param("studentId") Long studentId,
            @Param("guidanceStaffId") Long guidanceStaffId,
            @Param("statuses") List<String> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    boolean existsByGuidanceStaffIdAndAppointmentTypeAndStatusAndScheduledDateBetweenAndEndDateIsNull(
            Long guidanceStaffId,
            String appointmentType,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    boolean existsByGuidanceStaffIdAndAppointmentTypeAndStatusAndScheduledDateBetweenAndEndDateIsNotNull(
            Long guidanceStaffId,
            String appointmentType,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    boolean existsByGuidanceStaffIdAndStatusInAndScheduledDateBetween(
            Long guidanceStaffId,
            List<String> statuses,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Appointment> findByGuidanceStaff_IdAndAppointmentTypeAndScheduledDateBetween(
            Long guidanceStaffId,
            String appointmentType,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    boolean existsByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
            Long guidanceStaffId,
            List<String> statuses,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Appointment> findByStatus (String status);

    List<Appointment> findByStatusAndDateCreatedBefore(String status, LocalDateTime dateCreated);

    boolean existsByStudentIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
            Long studentId,
            List<String> statuses,
            LocalDateTime endDate,
            LocalDateTime startDate
    );

    List<Appointment> findByStudent_Id(Long studentId);

    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.student s " +
            "LEFT JOIN FETCH a.guidanceStaff g " +
            "WHERE a.status IN :statuses")
    List<Appointment> findByStatusInOptimized(@Param("statuses") List<String> statuses);

    List<Appointment> findByGuidanceStaff_IdAndScheduledDateBetween(
            Long id, LocalDateTime scheduledDate, LocalDateTime endDate);

    List<Appointment> findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
            Long guidanceStaffId, List<String> statuses, LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Appointment> findByStudent_IdAndStatusInIgnoreCase(Long studentId, List <String> status);

    long countByStudent_IdAndStatus(Long studentId, String status);

    List<Appointment> findByStatusAndScheduledDateBetween(
            String status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentType = :appointmentType " +
            "AND a.status = :status " +
            "AND (" +
            "  (a.endDate IS NOT NULL AND a.endDate < :nowUTC) " +
            "  OR (a.endDate IS NULL AND a.scheduledDate < :startOfToday)" +
            ")")
    List<Appointment> findExpiredAvailabilityBlocks(
            @Param("appointmentType") String appointmentType,
            @Param("status") String status,
            @Param("nowUTC") LocalDateTime nowUTC,
            @Param("startOfToday") LocalDateTime startOfToday
    );

    List<Appointment> findByStudent_IdAndStatusInAndScheduledDateBetween(
            Long studentId, List<String> statuses, LocalDateTime startOfDay, LocalDateTime endOfDay);


    boolean existsByGuidanceStaffIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
            Long staffId,
            List<String> status,
            LocalDateTime end,
            LocalDateTime start
    );


    @Query("SELECT a FROM Appointment a WHERE a.guidanceStaff.id = :employeeNumber " +
            "AND a.appointmentType = 'AVAILABILITY_BLOCK' " +
            "AND a.status = 'BLOCKED' " +
            "ORDER BY a.scheduledDate ASC")
    List<Appointment> findAvailabilityBlocksByGuidanceStaff(@Param("employeeNumber") Long employeeNumber);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.guidanceStaff.id = :employeeNumber
          AND a.appointmentType = 'AVAILABILITY_BLOCK'
          AND a.status = 'BLOCKED'
          AND a.scheduledDate BETWEEN :startDate AND :endDate
        ORDER BY a.scheduledDate ASC
    """)
    List<Appointment> findAvailabilityBlocksInDateRange(
            @Param("employeeNumber") Long employeeNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find blocks for a specific date (all day and partial blocks) using LocalDateTime range.
     */
    default List<Appointment> findBlocksByDate(Long employeeNumber, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return findAvailabilityBlocksInDateRange(employeeNumber, startOfDay, endOfDay);
    }

    /**
     * Find all appointments for a guidance staff, excluding availability blocks
     */
    @Query("""
    SELECT a FROM Appointment a
    WHERE a.guidanceStaff.id = :guidanceStaffId
      AND a.appointmentType != 'AVAILABILITY_BLOCK'
      AND a.status != 'BLOCKED'
    ORDER BY a.scheduledDate DESC
""")
    List<Appointment> findByGuidanceStaffExcludingBlocks(@Param("guidanceStaffId") Long guidanceStaffId);


    /**
     * Find appointments by guidance staff and multiple statuses, excluding availability blocks
     */
    @Query("""
    SELECT a FROM Appointment a
    WHERE a.guidanceStaff.id = :guidanceStaffId
      AND a.status IN :statuses
      AND a.appointmentType != 'AVAILABILITY_BLOCK'
    ORDER BY a.scheduledDate DESC
""")
    List<Appointment> findByGuidanceStaffAndStatusInExcludingBlocks(
            @Param("guidanceStaffId") Long guidanceStaffId,
            @Param("statuses") List<String> statuses
    );


}
