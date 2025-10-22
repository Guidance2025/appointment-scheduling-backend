package org.rocs.asa.repository.appointment;

import org.rocs.asa.domain.appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByStatusIgnoreCase(String status);

    boolean existsByStudentIdAndStatusIn(Long studentId, List<String> status);

    List<Appointment> findByStatusIn(List<String> status);
    List<Appointment> findByGuidanceStaff_Id(Long guidanceStaffId);

    List<Appointment> findByGuidanceStaff_IdAndStatusIgnoreCase(Long guidanceStaffId, String status);

}
