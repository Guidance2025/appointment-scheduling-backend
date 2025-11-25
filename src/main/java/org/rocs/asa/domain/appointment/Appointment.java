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
@Table(name = "tbl_appointment", indexes = {
        @Index(name = "idx_status_dates", columnList = "status, scheduled_date, end_date")
})
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
    private GuidanceStaff guidanceStaff;

    @OneToOne
    @JoinColumn(name = "login_id")
    @JsonIgnore
    private User user;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "appointment_type")
    private String appointmentType;

    @Column(name = "status")
    private String status;

    @Column(name = "notes")
    private String notes;
}