package org.rocs.asa.service.exit.interview.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.rocs.asa.domain.question.Question;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.dto.exit.ExitInterviewDetailDto;
import org.rocs.asa.dto.exit.StudentListRow;
import org.rocs.asa.repository.exit.interview.ExitInterviewRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.exit.ExitInterviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExitInterviewServiceImpl implements ExitInterviewService {

    private final StudentRepository studentRepository;
    private final ExitInterviewRepository exitInterviewRepository;

    @PersistenceContext
    private EntityManager em;

    public ExitInterviewServiceImpl(StudentRepository studentRepository,
                                    ExitInterviewRepository exitInterviewRepository) {
        this.studentRepository = studentRepository;
        this.exitInterviewRepository = exitInterviewRepository;
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

        var all = exitInterviewRepository.findAllByStudent_IdOrderBySubmittedDateDesc(studentId);

        var answers = all.stream().map(ei ->
                new ExitInterviewDetailDto.AnswerItem(
                        ei.getQuestion() != null ? ei.getQuestion().getQuestionText() : null,
                        ei.getResponseText()
                )).toList();

        LocalDateTime submitted = all.stream()
                .map(ExitInterview::getSubmittedDate)
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
        ExitInterview ei = new ExitInterview();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Question question = em.find(Question.class, questionId);

        ei.setStudent(student);
        ei.setQuestion(question);
        ei.setResponseText(responseText);
        ei.setSubmittedDate(LocalDate.now());

        exitInterviewRepository.save(ei);
    }
}

