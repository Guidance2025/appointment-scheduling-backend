package org.rocs.asa.service.exit.interview.impl;

import com.google.api.gax.rpc.NotFoundException;
import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.rocs.asa.domain.exit.request.ExitInterviewRequest;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.student.information.response.StudentDetailsResponse;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.EmptyFieldException;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.exception.domain.QuestionDoesNotExistException;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.exit.interview.ExitInterviewRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.questions.QuestionsRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.exit.interview.ExitInterviewService;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.notification.NotificationService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExitInterviewServiceImpl implements ExitInterviewService {
    private static Logger LOGGER = LoggerFactory.getLogger(ExitInterviewServiceImpl.class);

    private GuidanceStaffRepository guidanceStaffRepository;
    private QuestionsRepository questionsRepository;
    private ExitInterviewRepository exitInterviewRepository;
    private StudentRepository studentRepository;
    private StudentService studentService;
    private NotificationService notificationService;
    private UserRepository userRepository;
    private DeviceTokenRepository deviceTokenRepository;
    private CategoryRepository categoryRepository;
    private GuidanceService guidanceService;

    @Autowired
    public ExitInterviewServiceImpl(GuidanceStaffRepository guidanceStaffRepository,
                                    QuestionsRepository questionsRepository,
                                    ExitInterviewRepository exitInterviewRepository,
                                    StudentRepository studentRepository,
                                    StudentService studentService,
                                    NotificationService notificationService,
                                    UserRepository userRepository,
                                    DeviceTokenRepository deviceTokenRepository,
                                    CategoryRepository categoryRepository,
                                    GuidanceService guidanceService) {
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.questionsRepository = questionsRepository;
        this.exitInterviewRepository = exitInterviewRepository;
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
    public List<Questions> createMultipleExitInterviewQuestions(Long guidanceStaffId, List<String> questionTexts, List<Long> selectedStudentIds) {
        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(guidanceStaffId)
                .orElseThrow(() -> new GuidanceStaffNotFoundException("Guidance Staff not found with id: " + guidanceStaffId));

        List<String> cleanedQuestions = questionTexts.stream()
                .filter(text -> text != null && !text.trim().isEmpty())
                .toList();

        if (cleanedQuestions.size() > 5) {
            throw new IllegalArgumentException("You can only create up to 5 questions.");
        }

        Category category = categoryRepository.findByCategoryNameIgnoreCase("Exit Interview")
                .orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setCategoryName("Exit Interview");
                    return categoryRepository.save(newCat);
                });

        List<Questions> questions = cleanedQuestions.stream()
                .map(text -> {
                    Questions q = new Questions();
                    q.setGuidanceStaff(guidanceStaff);
                    q.setQuestionText(text);
                    q.setDateCreated(LocalDateTime.now());
                    q.setCategory(category);
                    return q;
                })
                .toList();

        List<Questions> savedQuestions = questionsRepository.saveAll(questions);

        List<String> studentUserIds;
        if (selectedStudentIds != null && !selectedStudentIds.isEmpty()) {
            List<Student> targetStudents = studentRepository.findAllById(selectedStudentIds);
            studentUserIds = targetStudents.stream()
                    .map(s -> s.getUser().getUserId())
                    .collect(Collectors.toList());
            LOGGER.info("Created {} questions for {} selected students", savedQuestions.size(), targetStudents.size());
        } else {
            studentUserIds = userRepository.findAllByRole(Role.STUDENT_ROLE.name())
                    .stream()
                    .map(User::getUserId)
                    .toList();
            LOGGER.info("Created {} questions for all students", savedQuestions.size());
        }

        for (Questions q : savedQuestions) {
            String actionType = "EXIT_INTERVIEW_NEW_QUESTION_" + q.getId();
            if (!studentUserIds.isEmpty()) {
                notificationService.sendNotificationToAllStudent(
                        studentUserIds,
                        "New Exit Interview Questions",
                        "Posted new exit interview questions.",
                        actionType
                );
            }
        }

        return savedQuestions;
    }

    @Override
    public List<Questions> findByGuidanceStaffId(Long guidanceStaffId) {
        return questionsRepository.findByGuidanceStaffId(guidanceStaffId);
    }

    @Override
    public List<Questions> findAllQuestions() {
        return questionsRepository.findByCategoryName("Exit Interview");
    }

    @Override
    public ExitInterview studentResponse(ExitInterviewRequest request) {
        if (request.getQuestionId() == null) {
            throw new EmptyFieldException("Question ID cannot be null");
        }
        if (request.getResponseText() == null || request.getResponseText().trim().isEmpty()) {
            throw new EmptyFieldException("Response text cannot be empty");
        }
        Student authenticatedStudent = studentService.findByAuthenticatedStudent();

        Questions question = questionsRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new QuestionDoesNotExistException("Question does not exist"));

        boolean checkAnsweredQuestion = exitInterviewRepository.existsByStudentIdAndQuestionId(
                authenticatedStudent.getId(),
                request.getQuestionId()
        );

        if (checkAnsweredQuestion) {
            throw new QuestionDoesNotExistException("You have already answered this question");
        }

        ExitInterview saved = new ExitInterview();
        saved.setResponseText(request.getResponseText());
        saved.setQuestion(question);
        saved.setStudent(authenticatedStudent);
        saved.setSubmittedDate(LocalDateTime.now());
        LOGGER.info("Exit Interview Response Submitted Successfully");

        return exitInterviewRepository.save(saved);
    }

    @Override
    public List<ExitInterview> retrieveStudentResponse() {
        return exitInterviewRepository.findAll();
    }

    @Override
    public List<Questions> getUnansweredQuestionsForAuthenticatedStudent() {
        Student student = studentService.findByAuthenticatedStudent();
        String userId = student.getUser().getUserId();

        List<Questions> unansweredQuestions = questionsRepository.findUnansweredExitInterviewByStudentId(
                "Exit Interview",
                student.getId(),
                userId
        );

        return unansweredQuestions;
    }

    @Override
    @Transactional
    public Questions updateQuestion(Long questionId, String questionText) {
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
    @Override
    public List<StudentDetailsResponse> getAllStudentsForSelection() {
        List<Student> students = studentRepository.findStudentsWithValidData();  // Changed: Use custom query
        LOGGER.info("Fetched {} valid students from DB", students.size());

        return students.stream()
                .map(s -> {
                    StudentDetailsResponse dto = new StudentDetailsResponse();
                    dto.setId(s.getId());
                    dto.setStudentNumber(s.getStudentNumber() != null ? s.getStudentNumber() : "N/A");
                    dto.setFirstName(s.getPerson().getFirstName() != null ? s.getPerson().getFirstName() : "N/A");
                    dto.setLastName(s.getPerson().getLastName() != null ? s.getPerson().getLastName() : "N/A");
                    dto.setSectionName(s.getSection().getSectionName() != null ? s.getSection().getSectionName() : "N/A");
                    return dto;
                })
                .collect(Collectors.toList());
    }
}