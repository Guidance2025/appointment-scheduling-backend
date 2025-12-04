//package org.rocs.asa.service.questions.impl;
//
//import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
//import org.rocs.asa.domain.questions.Questions;
//import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
//import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
//import org.rocs.asa.repository.questions.QuestionsRepository;
//import org.rocs.asa.repository.student.StudentRepository;
//import org.rocs.asa.service.questions.QuestionsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class QuestionsServiceImpl implements QuestionsService {
//
//    private GuidanceStaffRepository guidanceStaffRepository;
//    private QuestionsRepository questionsRepository;
//    private StudentRepository studentRepository;
//
//    @Autowired
//    public QuestionsServiceImpl(GuidanceStaffRepository guidanceStaffRepository, QuestionsRepository questionsRepository, StudentRepository studentRepository) {
//        this.guidanceStaffRepository = guidanceStaffRepository;
//        this.questionsRepository = questionsRepository;
//        this.studentRepository = studentRepository;
//    }
//
//    @Override
//    @Transactional
//    public List<Questions> createMultipleSelfAssessmentQuestions(Long guidanceStaffId, List<String> questionTexts
//    ){
//        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(guidanceStaffId)
//                .orElseThrow(() -> new GuidanceStaffNotFoundException(
//                        "Guidance Staff not found with id: " + guidanceStaffId));
//
//        List<String> cleanedQuestions = questionTexts.stream()
//                .filter(text -> text != null && !text.trim().isEmpty())
//                .toList();
//
//        if (cleanedQuestions.size() > 5) {
//            throw new IllegalArgumentException("You can only create up to 5 questions.");
//        }
//
//        List<Questions> questions = cleanedQuestions.stream()
//                .map(text -> {
//                    Questions saveQuestion = new Questions();
//                    saveQuestion.setGuidanceStaff(guidanceStaff);
//                    saveQuestion.setQuestionText(text);
//                    saveQuestion.setDateCreated(LocalDateTime.now());
//                    return saveQuestion;
//                })
//                .collect(Collectors.toList());
//
//        return questionsRepository.saveAll(questions);
//    }
//
//}
