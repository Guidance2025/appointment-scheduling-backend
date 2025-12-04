package org.rocs.asa.domain.self.assesment;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "tbl_self_assessment")
public class SelfAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_response_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Questions question;

    private String responseText;

    private LocalDateTime responseDate;

}
