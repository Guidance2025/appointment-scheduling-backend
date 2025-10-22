package org.rocs.asa.repository.mood;

import org.rocs.asa.domain.mood.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findByStudent_StudentId(Long studentId);
    List<Mood> findByStudent_StudentIdAndEntryDateBetween(Long studentId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT m.entryDate.date, COUNT(m) FROM Mood m WHERE m.student.studentId = :studentId GROUP BY m.entryDate.date ORDER BY m.entryDate.date DESC")
    List<Object[]> findMoodCountsByStudentAndDate(Long studentId);
}