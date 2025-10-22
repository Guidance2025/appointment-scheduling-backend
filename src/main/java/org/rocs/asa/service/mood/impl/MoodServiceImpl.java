package org.rocs.asa.service.mood.impl;

import org.rocs.asa.domain.mood.Mood;
import org.rocs.asa.exception.domain.StudentNotFoundException;
import org.rocs.asa.repository.mood.MoodRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.mood.MoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MoodServiceImpl implements MoodService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoodServiceImpl.class);

    private final MoodRepository moodRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public MoodServiceImpl(MoodRepository moodRepository, StudentRepository studentRepository) {
        this.moodRepository = moodRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Mood> getMoodsByStudent(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
        return moodRepository.findByStudent_StudentId(studentId);
    }

    @Override
    public List<Mood> getMoodsByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return moodRepository.findByStudent_StudentIdAndEntryDateBetween(studentId, start, end);
    }

    @Override
    public Map<LocalDate, Long> getMoodSummaryByStudent(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
        List<Object[]> results = moodRepository.findMoodCountsByStudentAndDate(studentId);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> (Long) row[1]
                ));
    }
}