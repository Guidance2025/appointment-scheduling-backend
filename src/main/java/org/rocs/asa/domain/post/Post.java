package org.rocs.asa.domain.post;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "employee_number", nullable = false)
    private Long employeeNumber;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "post_content", length = 500, nullable = false)
    private String postContent;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(Long employeeNumber) { this.employeeNumber = employeeNumber; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }
    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }
}
