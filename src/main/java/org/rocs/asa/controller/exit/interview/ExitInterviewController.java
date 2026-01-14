package org.rocs.asa.controller.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterviewQuestion;
import org.rocs.asa.domain.exit.interview.ExitInterviewResponse;
import org.rocs.asa.dto.exit.ExitInterviewDetailDto;
import org.rocs.asa.dto.exit.ExitInterviewQuestionRequest;
import org.rocs.asa.dto.exit.ExitInterviewResponseRequest;
import org.rocs.asa.dto.exit.StudentListRow;
import org.rocs.asa.service.exit.ExitInterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exit-interview")
public class ExitInterviewController {

    private final ExitInterviewService exitInterviewService;

    public ExitInterviewController(ExitInterviewService exitInterviewService) {
        this.exitInterviewService = exitInterviewService;
    }

    @PostMapping("/create/{id}")
    public ResponseEntity<List<ExitInterviewQuestion>> createMultipleQuestions(@PathVariable Long id, @RequestBody List<String> questionTexts) {
        List<ExitInterviewQuestion> questions = exitInterviewService.createMultipleExitInterviewQuestions(id, questionTexts);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/retrieve-questions/{id}")
    public ResponseEntity<List<ExitInterviewQuestion>> findAllQuestionByStaff(@PathVariable Long id) {
        List<ExitInterviewQuestion> staffQuestions = exitInterviewService.findByGuidanceStaffId(id);
        return ResponseEntity.ok(staffQuestions);
    }

    @GetMapping("/student/all-questions")
    public ResponseEntity<List<ExitInterviewQuestion>> findAllQuestionByStudent() {
        List<ExitInterviewQuestion> studentQuestions = exitInterviewService.findAllQuestions();
        return ResponseEntity.ok(studentQuestions);
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<ExitInterviewResponse> submitAnswer(@RequestBody ExitInterviewResponseRequest request) {
        ExitInterviewResponse response = exitInterviewService.studentResponse(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student-response")
    public ResponseEntity<List<ExitInterviewResponse>> retrieveStudentResponse() {
        List<ExitInterviewResponse> studentResponse = exitInterviewService.retrieveStudentResponse();
        return ResponseEntity.ok(studentResponse);
    }

    @GetMapping("/questions/unanswered")
    public ResponseEntity<List<ExitInterviewQuestion>> getUnansweredQuestion() {
        List<ExitInterviewQuestion> unansweredQuestion = exitInterviewService.getUnansweredQuestionsForAuthenticatedStudent();
        return ResponseEntity.ok(unansweredQuestion);
    }

    @PostMapping("/questions/{staffId}")
    public ResponseEntity<ExitInterviewQuestion> createQuestion(
            @PathVariable Long staffId,
            @RequestBody ExitInterviewQuestionRequest request) {
        ExitInterviewQuestion question = exitInterviewService.createQuestion(staffId, request);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/questions/{staffId}")
    public ResponseEntity<List<ExitInterviewQuestion>> getQuestions(@PathVariable Long staffId) {
        List<ExitInterviewQuestion> questions = exitInterviewService.getQuestions(staffId);
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<ExitInterviewQuestion> updateQuestion(
            @PathVariable Long id,
            @RequestBody ExitInterviewQuestionRequest request) {
        ExitInterviewQuestion question = exitInterviewService.updateQuestion(id, request);
        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        exitInterviewService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/responses")
    public ResponseEntity<List<ExitInterviewResponse>> getResponses() {
        List<ExitInterviewResponse> responses = exitInterviewService.getResponses();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/responses/{studentId}")
    public ResponseEntity<ExitInterviewResponse> submitResponse(
            @PathVariable Long studentId,
            @RequestBody ExitInterviewResponseRequest request) {
        ExitInterviewResponse response = exitInterviewService.submitResponse(studentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentListRow>> getStudents(
            @RequestParam(defaultValue = "All") String course,
            @RequestParam(defaultValue = "All") String cluster) {
        List<StudentListRow> students = exitInterviewService.getStudents(course, cluster);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<ExitInterviewDetailDto> getStudentDetail(@PathVariable Long studentId) {
        ExitInterviewDetailDto detail = exitInterviewService.getStudentDetail(studentId);
        return ResponseEntity.ok(detail);
    }

    private Long getAuthenticatedStaffId() {
        throw new UnsupportedOperationException("Implement authentication to get guidance staff ID");
    }
}