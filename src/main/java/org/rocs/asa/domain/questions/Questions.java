package org.rocs.asa.domain.questions;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_questions")
public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "employee_number")
    private GuidanceStaff guidanceStaff;

    private String questionText;

    private LocalDateTime dateCreated;
}
