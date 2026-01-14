package org.rocs.asa.repository.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterviewResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitInterviewResponseRepository extends JpaRepository<ExitInterviewResponse, Long> {
    List<ExitInterviewResponse> findAllByStudent_IdOrderBySubmittedDateDesc(Long studentId);
    boolean existsByStudentIdAndQuestionId(Long studentId, Long questionId);
    List<ExitInterviewResponse> findByQuestionId(Long questionId);
}