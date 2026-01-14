package org.rocs.asa.service.mood.trend;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.repository.mood.trend.MoodTrendRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodTrendService {

    private final MoodTrendRepository moodTrendRepository;

    public MoodTrendService(MoodTrendRepository moodTrendRepository) {
        this.moodTrendRepository = moodTrendRepository;
    }

    public List<Mood> getAllMoods() {
        return moodTrendRepository.findAll();
    }
}