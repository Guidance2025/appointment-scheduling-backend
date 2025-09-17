package org.rocs.asa.domain.appointment;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tbl_appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "student_id")
    private Student student;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_number")
    private GuidanceStaff guidanceStaff;

    private LocalDateTime scheduledDate;

    private LocalDateTime endDate;

    private LocalDateTime dateCreated;

    private String appointmentType;

    private String status;

    private String notes;
}
