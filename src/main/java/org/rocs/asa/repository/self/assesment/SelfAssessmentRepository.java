package org.rocs.asa.repository.self.assesment;

import org.rocs.asa.domain.self.assesment.SelfAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SelfAssessmentRepository extends JpaRepository<SelfAssessment,Long> {
    boolean existsByStudentIdAndQuestionId(Long studentId, Long questionId);
}
