package org.rocs.asa.domain.mood;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tbl_moods")
public class Mood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mood_id")
    private Long moodId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "mood", nullable = false)
    private String mood;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "mood_notes")
    private String moodNotes;
}