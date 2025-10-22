package org.rocs.asa.service.question;

import org.rocs.asa.domain.question.Question;

import java.util.List;

public interface QuestionService {
    Question createQuestion(Question question, Long employeeNumber);
    Question updateQuestion(Long questionId, Question question, Long employeeNumber);
    void deleteQuestion(Long questionId, Long employeeNumber);
    List<Question> getQuestionsByEmployee(Long employeeNumber);
    Question getQuestionByIdAndEmployee(Long questionId, Long employeeNumber);
}