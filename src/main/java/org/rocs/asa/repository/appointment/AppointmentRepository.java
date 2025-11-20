package org.rocs.asa.repository.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByStudentIdAndStatusIn(Long studentId, List<String> status);

    List<Appointment> findByGuidanceStaff_Id(Long guidanceStaffId);

    List<Appointment> findByGuidanceStaff_IdAndStatusIgnoreCase(Long guidanceStaffId, String status);

    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.student s " +
            "LEFT JOIN FETCH a.guidanceStaff g " +
            "WHERE a.status IN :statuses")
    List<Appointment> findByStatusInOptimized(@Param("statuses") List<String> statuses);

}
