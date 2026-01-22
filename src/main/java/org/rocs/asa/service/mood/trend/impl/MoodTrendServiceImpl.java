package org.rocs.asa.service.mood.impl;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.dto.MoodEntryRequest;
import org.rocs.asa.repository.mood.trend.MoodTrendRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.mood.MoodTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MoodTrendServiceImpl implements MoodTrendService {

    @Autowired
    private MoodTrendRepository moodTrendRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    @Transactional
    public Mood saveMoodEntry(MoodEntryRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Mood mood = new Mood();
        mood.setStudent(student);
        mood.setEmotions(request.getEmotions());
        mood.setMoodNotes(request.getNote());

        return moodTrendRepository.save(mood);
    }

    @Override
    public List<Mood> getAllMoodEntries() {
        return moodTrendRepository.findAll();
    }

    @Override
    public List<Mood> getMoodEntriesByStudent(Long studentId) {
        return moodTrendRepository.findByStudentIdOrderByEntryDateDesc(studentId);
    }
}