package org.rocs.asa.domain.post;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "tbl_posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "employee_number", nullable = false)
    private Long employeeNumber;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "post_content", length = 500, nullable = false)
    private String postContent;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    // Single sectionId instead of multiple sections
    @Column(name = "section_id")
    private Long sectionId;
}