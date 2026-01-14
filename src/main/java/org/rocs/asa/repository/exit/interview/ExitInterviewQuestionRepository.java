package org.rocs.asa.repository.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitInterviewQuestionRepository extends JpaRepository<ExitInterviewQuestion, Long> {
    List<ExitInterviewQuestion> findByGuidanceStaffId(Long guidanceStaffId);

    @Query("SELECT q FROM ExitInterviewQuestion q WHERE q.id NOT IN " +
            "(SELECT r.question.id FROM ExitInterviewResponse r WHERE r.student.id = :studentId)")
    List<ExitInterviewQuestion> findUnansweredQuestionByStudentId(@Param("studentId") Long studentId);
}