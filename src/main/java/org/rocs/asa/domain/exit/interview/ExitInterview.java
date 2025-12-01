package org.rocs.asa.domain.exit.interview;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.question.Question;

import java.time.LocalDate;

@Entity @Table(name = "tbl_exit_interview")
@Getter @Setter
public class ExitInterview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id") private Long id;

    @ManyToOne @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "response_text") private String responseText;
    @Column(name = "submitted_date") private LocalDate submittedDate;
}
