package org.rocs.asa.domain.question;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_text", length = 255)
    private String questionText;

    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String v) { this.questionText = v; }
}

