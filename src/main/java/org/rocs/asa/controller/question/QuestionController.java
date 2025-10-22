package org.rocs.asa.controller.question;

import jakarta.validation.Valid;
import org.rocs.asa.domain.question.Question;
import org.rocs.asa.service.question.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guidance/questions")
@CrossOrigin("*")
public class QuestionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionController.class);
    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/create")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody Question question, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication); // Implement based on your UserPrincipal
        Question createdQuestion = questionService.createQuestion(question, employeeNumber);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    @PutMapping("/update/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @Valid @RequestBody Question question, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        Question updatedQuestion = questionService.updateQuestion(questionId, question, employeeNumber);
        return ResponseEntity.ok(updatedQuestion);
    }

    @DeleteMapping("/delete/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        questionService.deleteQuestion(questionId, employeeNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<Question>> getQuestions(Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        List<Question> questions = questionService.getQuestionsByEmployee(employeeNumber);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<Question> getQuestion(@PathVariable Long questionId, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        Question question = questionService.getQuestionByIdAndEmployee(questionId, employeeNumber);
        return ResponseEntity.ok(question);
    }

    private Long getEmployeeNumberFromAuth(Authentication authentication) {
        throw new UnsupportedOperationException("Implement based on your authentication setup");
    }
}