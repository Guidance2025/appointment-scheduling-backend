package org.rocs.asa.domain.mood;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@Entity
@Table(name = "tbl_moods")
public class Mood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mood_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "mood", nullable = false)
    private String mood;  // Comma-separated emotions, e.g., "happy,calm"

    @Column(name = "mood_notes", length = 128)
    private String moodNotes;  // Optional note

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate = LocalDateTime.now();

    public List<String> getEmotions() {
        return mood != null ? Arrays.asList(mood.split(",")) : List.of();
    }

    public void setEmotions(List<String> emotions) {
        this.mood = String.join(",", emotions);
    }
}