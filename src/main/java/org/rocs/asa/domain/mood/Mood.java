package org.rocs.asa.domain.mood;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_moods")
@Data
public class Mood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mood_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String mood;
    private LocalDateTime entryDate;
    private String moodNotes;
}