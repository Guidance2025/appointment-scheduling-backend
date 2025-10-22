package org.rocs.asa.domain.appointment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tbl_appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "employee_number")
    @JsonIgnore
    private GuidanceStaff guidanceStaff;

    @OneToOne
    @JoinColumn(name = "login_id")
    @JsonIgnore
    private User user;

    private LocalDateTime scheduledDate;

    private LocalDateTime endDate;

    private LocalDateTime dateCreated;

    private String appointmentType;

    private String status;

    private String notes;
}
