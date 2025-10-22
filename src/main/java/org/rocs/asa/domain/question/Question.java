package org.rocs.asa.domain.question;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "tbl_questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "employee_number")
    private GuidanceStaff guidanceStaff;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "date_created", nullable = false)
    private LocalDate dateCreated;
}