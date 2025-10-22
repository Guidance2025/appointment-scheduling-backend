package org.rocs.asa.controller.mood;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.service.mood.MoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/guidance/moods")
@CrossOrigin("*")
public class MoodController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoodController.class);
    private final MoodService moodService;

    @Autowired
    public MoodController(MoodService moodService) {
        this.moodService = moodService;
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Mood>> getMoodsByStudent(@PathVariable Long studentId) {
        List<Mood> moods = moodService.getMoodsByStudent(studentId);
        return ResponseEntity.ok(moods);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/student/{studentId}/range")
    public ResponseEntity<List<Mood>> getMoodsByDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Mood> moods = moodService.getMoodsByStudentAndDateRange(studentId, startDate, endDate);
        return ResponseEntity.ok(moods);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<Map<LocalDate, Long>> getMoodSummary(@PathVariable Long studentId) {
        Map<LocalDate, Long> summary = moodService.getMoodSummaryByStudent(studentId);
        return ResponseEntity.ok(summary);
    }
}