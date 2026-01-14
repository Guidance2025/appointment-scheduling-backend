package org.rocs.asa.repository.mood.trend;

import org.rocs.asa.domain.mood.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoodTrendRepository extends JpaRepository<Mood, Long> {
}