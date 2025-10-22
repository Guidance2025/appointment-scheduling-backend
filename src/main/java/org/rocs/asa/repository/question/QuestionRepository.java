package org.rocs.asa.repository.question;

import org.rocs.asa.domain.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategory_CategoryIdAndGuidanceStaff_EmployeeNumber(Long categoryId, Long employeeNumber);
    boolean existsByQuestionIdAndGuidanceStaff_EmployeeNumber(Long questionId, Long employeeNumber);
}