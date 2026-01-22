package org.rocs.asa.repository.mood.trend;

import org.rocs.asa.domain.mood.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoodTrendRepository extends JpaRepository<Mood, Long> {
    List<Mood> findByStudentId(Long studentId);

    @Query("SELECT m FROM Mood m WHERE m.student.id = :studentId ORDER BY m.entryDate DESC")
    List<Mood> findByStudentIdOrderByEntryDateDesc(@Param("studentId") Long studentId);
}