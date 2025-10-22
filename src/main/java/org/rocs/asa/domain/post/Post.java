package org.rocs.asa.domain.post;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.question.Question;
import org.rocs.asa.domain.section.Section;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tbl_posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "employee_number")
    private GuidanceStaff guidanceStaff;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(optional = true)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "post_content", nullable = false)
    private String postContent;

    @Column(name = "posted_date", nullable = false)
    private LocalDateTime postedDate;
}
