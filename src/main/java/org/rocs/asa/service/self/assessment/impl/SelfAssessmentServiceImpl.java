package org.rocs.asa.service.self.assessment.impl;

import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.self.assesment.SelfAssessment;
import org.rocs.asa.domain.self.request.SelfAssessmentRequest;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.EmptyFieldException;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.exception.domain.QuestionDoesNotExistException;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.questions.QuestionsRepository;
import org.rocs.asa.repository.self.assesment.SelfAssessmentRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.self.assessment.SelfAssesmentService;
import org.rocs.asa.service.student.StudentService;
import org.rocs.asa.domain.category.Category;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.utils.security.enumeration.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SelfAssessmentServiceImpl implements SelfAssesmentService {
    private static Logger LOGGER = LoggerFactory.getLogger(SelfAssessment.class);

    private GuidanceStaffRepository guidanceStaffRepository;
    private QuestionsRepository questionsRepository;
    private SelfAssessmentRepository selfAssessmentRepository;
    private StudentRepository studentRepository;
    private StudentService studentService;
    private NotificationService notificationService;
    private UserRepository userRepository;
    private DeviceTokenRepository deviceTokenRepository;
    private CategoryRepository categoryRepository;
    private GuidanceService guidanceService;

    @Autowired
    public SelfAssessmentServiceImpl(GuidanceStaffRepository guidanceStaffRepository,
                                     QuestionsRepository questionsRepository,
                                     SelfAssessmentRepository selfAssessmentRepository,
                                     StudentRepository studentRepository,
                                     StudentService studentService,
                                     NotificationService notificationService,
                                     UserRepository userRepository,DeviceTokenRepository deviceTokenRepository,
                                     CategoryRepository categoryRepository, GuidanceService guidanceService) {
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.questionsRepository = questionsRepository;
        this.selfAssessmentRepository = selfAssessmentRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.categoryRepository = categoryRepository;
        this.guidanceService = guidanceService;
    }

    @Override
    @Transactional
    public List<Questions> createMultipleSelfAssessmentQuestions(
            Long guidanceStaffId,
            List<String> questionTexts
    ) {
        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(guidanceStaffId)
                .orElseThrow(() -> new GuidanceStaffNotFoundException(
                        "Guidance Staff not found with id: " + guidanceStaffId));

        List<String> cleanedQuestions = questionTexts.stream()
                .filter(text -> text != null && !text.trim().isEmpty())
                .toList();

        if (cleanedQuestions.size() > 5) {
            throw new IllegalArgumentException("You can only create up to 5 questions.");
        }

        Category category = categoryRepository.findByCategoryNameIgnoreCase("SELF ASSESSMENT")
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setCategoryName("SELF ASSESSMENT");
                    return categoryRepository.save(newCategory);
                });

        List<Questions> questions = cleanedQuestions.stream()
                .map(text -> {
                    Questions question = new Questions();
                    question.setGuidanceStaff(guidanceStaff);
                    question.setQuestionText(text);
                    question.setDateCreated(LocalDateTime.now());
                    question.setCategory(category);
                    return question;
                })
                .toList();

        List<String> studentUserIds = userRepository.findAllByRole(Role.STUDENT_ROLE.name())
                .stream()
                .map(User::getUserId)
                .toList();

        notificationService.sendNotificationToAllStudent(
                studentUserIds,
                "New Self Assessment Questions",
                " Posted new self-assessment questions.",
                "SELF ASSESSMENT UPDATE"
        );

        return questionsRepository.saveAll(questions);
    }


    @Override
    public List<Questions> findByGuidanceStaffId(Long guidanceStaffId) {
        return questionsRepository.findByGuidanceStaffId(guidanceStaffId);
    }

    @Override
    public List<Questions> findAllQuestions() {
        return questionsRepository.findByCategoryName("SELF ASSESSMENT");
    }

    @Override
    public SelfAssessment studentResponse(SelfAssessmentRequest request) {
        if (request.getQuestionId() == null) {
            throw new EmptyFieldException("Question ID cannot be null");
        }
        if (request.getResponseText() == null || request.getResponseText().trim().isEmpty()) {
            throw new EmptyFieldException("Response text cannot be empty");
        }
        Student authenticatedStudent = studentService.findByAuthenticatedStudent();

        Questions question = questionsRepository.findById(request.getQuestionId()).orElseThrow
                (() -> new QuestionDoesNotExistException("Question does not exist"));

        boolean checkAnsweredQuestion = selfAssessmentRepository.existsByStudentIdAndQuestionId(authenticatedStudent.getId(), request.getQuestionId());

        if(checkAnsweredQuestion) {
            throw new QuestionDoesNotExistException("You have already answered this question");
        }


        SelfAssessment saved = new SelfAssessment();
        saved.setResponseText(request.getResponseText());
        saved.setQuestion(question);
        saved.setStudent(authenticatedStudent);
        saved.setResponseDate(LocalDateTime.now());
        LOGGER.info("Assessment Submit Successfully");

       return selfAssessmentRepository.save(saved);
    }

    @Override
    public List<SelfAssessment> retrieveStudentResponse() {
        return selfAssessmentRepository.findAll();
    }

    @Override
    public List<Questions> getUnansweredQuestionsForAuthenticatedStudent() {
        Student student = studentService.findByAuthenticatedStudent();
        return questionsRepository.findUnansweredSelfAssessmentByStudentId(student.getId(), "SELF ASSESSMENT");
    }

    @Override
    @Transactional
    public Questions updateQuestions(Long questionId, String questionText) {
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new EmptyFieldException("Question text cannot be null or empty");
        }

        GuidanceStaff authenticatedStaff = guidanceService.findAuthenticatedGuidanceStaff();

        Questions question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new QuestionDoesNotExistException("Question not found with id: " + questionId));

        if (!question.getGuidanceStaff().getId().equals(authenticatedStaff.getId())) {
            throw new GuidanceStaffNotFoundException("You are not authorized to update this question");
        }

        question.setQuestionText(questionText.trim());
        LOGGER.info("Question updated successfully for ID: {}", questionId);
        return questionsRepository.save(question);
    }
}
