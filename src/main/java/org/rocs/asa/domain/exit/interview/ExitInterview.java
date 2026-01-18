package org.rocs.asa.domain.exit.interview;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "tbl_exit_interview")
public class ExitInterview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Questions question;

    private String responseText;

    private LocalDate submittedDate;
}