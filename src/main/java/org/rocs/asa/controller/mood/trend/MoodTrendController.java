package org.rocs.asa.controller.mood.trend;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.service.mood.trend.MoodTrendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moods")
public class MoodTrendController {

    private final MoodTrendService moodTrendService;

    public MoodTrendController(MoodTrendService moodTrendService) {
        this.moodTrendService = moodTrendService;
    }

    @GetMapping
    public ResponseEntity<List<Mood>> getAllMoods() {
        List<Mood> moods = moodTrendService.getAllMoods();
        return ResponseEntity.ok(moods);
    }
}