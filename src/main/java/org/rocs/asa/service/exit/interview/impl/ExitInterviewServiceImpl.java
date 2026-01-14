package org.rocs.asa.service.exit.interview.impl;

import com.google.api.gax.rpc.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.rocs.asa.domain.exit.interview.ExitInterviewQuestion;
import org.rocs.asa.domain.exit.interview.ExitInterviewResponse;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.dto.exit.ExitInterviewDetailDto;
import org.rocs.asa.dto.exit.ExitInterviewQuestionRequest;
import org.rocs.asa.dto.exit.ExitInterviewResponseRequest;
import org.rocs.asa.dto.exit.StudentListRow;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.exception.domain.QuestionDoesNotExistException;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.exit.interview.ExitInterviewQuestionRepository;
import org.rocs.asa.repository.exit.interview.ExitInterviewResponseRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.questions.QuestionsRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.exit.ExitInterviewService;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.student.StudentService;
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
    private ExitInterviewQuestionRepository exitInterviewQuestionRepository;
    private ExitInterviewResponseRepository exitInterviewResponseRepository;
    private StudentRepository studentRepository;
    private StudentService studentService;
    private NotificationService notificationService;
    private UserRepository userRepository;
    private DeviceTokenRepository deviceTokenRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public ExitInterviewServiceImpl(GuidanceStaffRepository guidanceStaffRepository,
                                    ExitInterviewQuestionRepository exitInterviewQuestionRepository,
                                    ExitInterviewResponseRepository exitInterviewResponseRepository,
                                    StudentRepository studentRepository,
                                    StudentService studentService,
                                    NotificationService notificationService,
                                    UserRepository userRepository,
                                    DeviceTokenRepository deviceTokenRepository) {
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.exitInterviewQuestionRepository = exitInterviewQuestionRepository;
        this.exitInterviewResponseRepository = exitInterviewResponseRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public List<StudentListRow> getStudents(String course, String cluster) {
        var q = em.createNativeQuery("""
            select
              s.id as student_id,
              trim(p.first_name || ' ' || nvl(p.middle_name, '') || case when p.middle_name is not null then ' ' else '' end || p.last_name) as name,
              s.student_number as student_number,
              sec.course as course,
              sec.cluster_name as cluster_name,
              case when exists (select 1 from tbl_exit_interview ei where ei.student_id = s.id) then 1 else 0 end as has_response
            from tbl_student s
              join tbl_person p on p.id = s.person_id
              left join tbl_section sec on sec.section_id = s.section_id
            where (:course = 'All' or sec.course = :course)
              and (:cluster = 'All' or sec.cluster_name = :cluster)
            order by upper(p.last_name), upper(p.first_name)
        """);

        q.setParameter("course", course);
        q.setParameter("cluster", cluster);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        List<StudentListRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long studentId = r[0] == null ? null : ((Number) r[0]).longValue();
            String name = (String) r[1];
            String studentNumber = (String) r[2];
            String c = (String) r[3];
            String cl = (String) r[4];
            boolean hasResponse = r[5] != null && ((Number) r[5]).intValue() == 1;
            result.add(new StudentListRow(studentId, name, studentNumber, c, cl, hasResponse));
        }
        return result;
    }

    @Override
    public ExitInterviewDetailDto getStudentDetail(Long studentId) {
        Student s = studentRepository.findById(studentId).orElse(null);
        if (s == null) return new ExitInterviewDetailDto(null, null, List.of());

        var p = s.getPerson();
        String name = p == null ? null : (
                (p.getFirstName() != null ? p.getFirstName() : "") +
                        (p.getMiddleName() != null && !p.getMiddleName().isBlank() ? " " + p.getMiddleName() : "") +
                        (p.getLastName() != null ? " " + p.getLastName() : "")
        ).trim();

        String studentNumber = s.getStudentNumber();
        String course = s.getSection() != null ? s.getSection().getCourse() : null;
        String cluster = s.getSection() != null ? s.getSection().getClusterName() : null;

        var all = exitInterviewResponseRepository.findAllByStudent_IdOrderBySubmittedDateDesc(studentId);
        var answers = all.stream().map(ei ->
                new ExitInterviewDetailDto.AnswerItem(
                        ei.getQuestion() != null ? ei.getQuestion().getQuestionText() : null,
                        ei.getResponseText()
                )).toList();

        LocalDateTime submitted = all.stream()
                .map(ExitInterviewResponse::getSubmittedDate)
                .filter(d -> d != null)
                .map(d -> d.atStartOfDay())
                .findFirst().orElse(null);

        return new ExitInterviewDetailDto(
                new ExitInterviewDetailDto.StudentInfo(name, studentNumber, course, cluster),
                submitted,
                answers
        );
    }

    @Override
    @Transactional
    public void saveAnswer(Long studentId, Long questionId, String responseText) {
        ExitInterviewResponse ei = new ExitInterviewResponse();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        ExitInterviewQuestion question = exitInterviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        ei.setStudent(student);
        ei.setQuestion(question);
        ei.setResponseText(responseText);
        ei.setSubmittedDate(LocalDate.now());

        exitInterviewResponseRepository.save(ei);
    }

    @Override
    @Transactional
    public List<ExitInterviewQuestion> createMultipleExitInterviewQuestions(
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

        List<ExitInterviewQuestion> questions = cleanedQuestions.stream()
                .map(text -> {
                    ExitInterviewQuestion q = new ExitInterviewQuestion();
                    q.setGuidanceStaff(guidanceStaff);
                    q.setQuestionText(text);
                    q.setDateCreated(LocalDateTime.now());
                    return q;
                })
                .toList();

        List<String> studentUserIds = userRepository.findAllByRole(Role.STUDENT_ROLE.name())
                .stream()
                .map(User::getUserId)
                .toList();

        notificationService.sendNotificationToAllStudent(
                studentUserIds,
                "New Exit Interview Questions",
                "Posted new exit interview questions.",
                "EXIT INTERVIEW UPDATE"
        );

        return exitInterviewQuestionRepository.saveAll(questions);
    }

    @Override
    public List<ExitInterviewQuestion> findByGuidanceStaffId(Long guidanceStaffId) {
        return exitInterviewQuestionRepository.findByGuidanceStaffId(guidanceStaffId);
    }

    @Override
    public List<ExitInterviewQuestion> findAllQuestions() {
        return exitInterviewQuestionRepository.findAll();
    }

    @Override
    @Transactional
    public ExitInterviewResponse studentResponse(ExitInterviewResponseRequest request) {
        if (request.getQuestionId() == null) {
            throw new IllegalArgumentException("Question ID cannot be null");
        }
        if (request.getResponseText() == null || request.getResponseText().trim().isEmpty()) {
            throw new IllegalArgumentException("Response text cannot be empty");
        }
        Student authenticatedStudent = studentService.findByAuthenticatedStudent();

        ExitInterviewQuestion question = exitInterviewQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new QuestionDoesNotExistException("Question does not exist"));

        boolean checkAnsweredQuestion = exitInterviewResponseRepository.existsByStudentIdAndQuestionId(authenticatedStudent.getId(), request.getQuestionId());

        if (checkAnsweredQuestion) {
            throw new QuestionDoesNotExistException("You have already answered this question");
        }

        ExitInterviewResponse saved = new ExitInterviewResponse();
        saved.setResponseText(request.getResponseText());
        saved.setQuestion(question);
        saved.setStudent(authenticatedStudent);
        saved.setSubmittedDate(LocalDate.now());
        LOGGER.info("Exit Interview Response Submitted Successfully");

        return exitInterviewResponseRepository.save(saved);
    }

    @Override
    public List<ExitInterviewResponse> retrieveStudentResponse() {
        return exitInterviewResponseRepository.findAll();
    }

    @Override
    public List<ExitInterviewQuestion> getUnansweredQuestionsForAuthenticatedStudent() {
        Student student = studentService.findByAuthenticatedStudent();
        return exitInterviewQuestionRepository.findUnansweredQuestionByStudentId(student.getId());
    }

    @Override
    public List<ExitInterviewQuestion> getQuestions(Long staffId) {
        return findByGuidanceStaffId(staffId);
    }

    @Override
    @Transactional
    public ExitInterviewQuestion createQuestion(Long staffId, ExitInterviewQuestionRequest request) {
        GuidanceStaff staff = guidanceStaffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Guidance staff not found"));
        ExitInterviewQuestion q = new ExitInterviewQuestion();
        q.setGuidanceStaff(staff);
        q.setQuestionText(request.getQuestionText());
        q.setCategory(request.getCategory());
        q.setDateCreated(LocalDateTime.now());
        return exitInterviewQuestionRepository.save(q);
    }

    @Override
    @Transactional
    public ExitInterviewQuestion updateQuestion(Long id, ExitInterviewQuestionRequest request) {
        ExitInterviewQuestion q = exitInterviewQuestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        q.setQuestionText(request.getQuestionText());
        q.setCategory(request.getCategory());
        return exitInterviewQuestionRepository.save(q);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        exitInterviewQuestionRepository.deleteById(id);
    }

    @Override
    public List<ExitInterviewResponse> getResponses() {
        return exitInterviewResponseRepository.findAll();
    }

    @Override
    @Transactional
    public ExitInterviewResponse submitResponse(Long studentId, ExitInterviewResponseRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        ExitInterviewQuestion question = exitInterviewQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        ExitInterviewResponse r = new ExitInterviewResponse();
        r.setStudent(student);
        r.setQuestion(question);
        r.setResponseText(request.getResponseText());
        r.setSubmittedDate(LocalDate.now());
        return exitInterviewResponseRepository.save(r);
    }
}