package org.rocs.asa.domain.question;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import org.rocs.asa.domain.category.Category;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_questions")
@Getter @Setter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "employee_number")
    private Long employeeNumber;

    @Column(name = "question_text")
    private String questionText;

    @Column(name = "date_created")
    private LocalDate dateCreated;
}

