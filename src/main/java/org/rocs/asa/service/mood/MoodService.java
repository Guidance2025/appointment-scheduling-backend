package org.rocs.asa.service.mood;

import org.rocs.asa.domain.mood.Mood;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MoodService {
    List<Mood> getMoodsByStudent(Long studentId);
    List<Mood> getMoodsByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate);
    Map<LocalDate, Long> getMoodSummaryByStudent(Long studentId);  // Daily count summary for monitoring
}