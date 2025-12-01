package org.rocs.asa.controller.question;

import jakarta.validation.Valid;
import org.rocs.asa.dto.question.CreateOrUpdateQuestionRequest;
import org.rocs.asa.dto.question.QuestionDto;
import org.rocs.asa.service.question.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exit-interview/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDto>> list() {
        return ResponseEntity.ok(questionService.list());
    }

    @PostMapping
    public ResponseEntity<QuestionDto> create(@Valid @RequestBody CreateOrUpdateQuestionRequest req) {
        return ResponseEntity.ok(questionService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> update(@PathVariable Long id,
                                              @Valid @RequestBody CreateOrUpdateQuestionRequest req) {
        return ResponseEntity.ok(questionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
