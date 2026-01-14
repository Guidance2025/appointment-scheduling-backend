package org.rocs.asa.domain.exit.interview;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_questions")
@Data
public class ExitInterviewQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_number")
    private GuidanceStaff guidanceStaff;

    private String questionText;
    private String category;  // e.g., "Exit Interview"
    private LocalDateTime dateCreated;
}