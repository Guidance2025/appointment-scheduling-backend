package org.rocs.asa.controller.mood.trend;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.dto.MoodEntryRequest;
import org.rocs.asa.service.mood.MoodTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/moods")
@CrossOrigin("*")
public class MoodTrendController {

    @Autowired
    private MoodTrendService moodTrendService;

    @PostMapping
    public ResponseEntity<Mood> saveMoodEntry(@Valid @RequestBody MoodEntryRequest request) {
        Mood savedEntry = moodTrendService.saveMoodEntry(request);
        return ResponseEntity.ok(savedEntry);
    }

    @GetMapping
    public ResponseEntity<List<Mood>> getAllMoodEntries() {
        List<Mood> entries = moodTrendService.getAllMoodEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Mood>> getMoodEntriesByStudent(@PathVariable Long studentId) {
        List<Mood> entries = moodTrendService.getMoodEntriesByStudent(studentId);
        return ResponseEntity.ok(entries);
    }
}