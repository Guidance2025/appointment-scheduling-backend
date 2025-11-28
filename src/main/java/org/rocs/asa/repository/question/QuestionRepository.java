package org.rocs.asa.repository.question;

import org.rocs.asa.dto.QuestionDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuestionRepository {
    List<QuestionDto> findAllByCategory(Long categoryId);
    Optional<QuestionDto> findByIdAndCategory(Long id, Long categoryId);
    Long insertQuestion(Long categoryId, Long employeeNumber, String questionText);
    void updateQuestionText(Long questionId, String questionText);
    void deleteQuestion(Long questionId);
    long countAnswersForQuestion(Long questionId);

    void assertBelongsToCategoryOrThrow(Long questionId, Long categoryId);
    void assertAllBelongToCategoryOrThrow(Set<Long> questionIds, Long categoryId);

    Collection<Object> findByCategory_NameIgnoreCaseOrderByQuestionIdAsc(String exitInterview);
}

