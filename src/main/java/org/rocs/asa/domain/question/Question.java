package org.rocs.asa.domain.question;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "employee_number")
    private Long employeeNumber;

    @Column(name = "question_text", length = 255)
    private String questionText;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    public Long getQuestionId() { return questionId; }
    public Long getCategoryId() { return categoryId; }
    public Long getEmployeeNumber() { return employeeNumber; }
    public String getQuestionText() { return questionText; }
    public LocalDate getDateCreated() { return dateCreated; }

    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setEmployeeNumber(Long employeeNumber) { this.employeeNumber = employeeNumber; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setDateCreated(LocalDate dateCreated) { this.dateCreated = dateCreated; }
}