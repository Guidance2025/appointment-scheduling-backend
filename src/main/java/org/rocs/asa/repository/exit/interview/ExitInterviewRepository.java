package org.rocs.asa.repository.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExitInterviewRepository extends JpaRepository<ExitInterview, Long> {
    boolean existsByStudentIdAndQuestionId(Long studentId, Long questionId);
}