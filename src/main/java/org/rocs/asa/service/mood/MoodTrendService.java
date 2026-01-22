package org.rocs.asa.service.mood;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.dto.MoodEntryRequest;

import java.util.List;

public interface MoodTrendService {
    Mood saveMoodEntry(MoodEntryRequest request);
    List<Mood> getAllMoodEntries();
    List<Mood> getMoodEntriesByStudent(Long studentId);
}