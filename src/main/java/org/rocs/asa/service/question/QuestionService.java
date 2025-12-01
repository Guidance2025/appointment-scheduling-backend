package org.rocs.asa.service.question;

import org.rocs.asa.dto.question.CreateOrUpdateQuestionRequest;
import org.rocs.asa.dto.question.QuestionDto;

import java.util.List;

public interface QuestionService {
    List<QuestionDto> list();
    QuestionDto create(CreateOrUpdateQuestionRequest req);
    QuestionDto update(Long id, CreateOrUpdateQuestionRequest req);
    void delete(Long id);
}
