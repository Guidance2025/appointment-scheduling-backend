package org.rocs.asa.controller.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.rocs.asa.domain.exit.request.ExitInterviewRequest;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.student.information.response.StudentDetailsResponse;
import org.rocs.asa.service.exit.interview.ExitInterviewService;
import org.rocs.asa.service.student.inforamation.StudentInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exit-interview")
@CrossOrigin("*")
public class ExitInterviewController {
    private ExitInterviewService exitInterviewService;

    @Autowired
    public ExitInterviewController(ExitInterviewService exitInterviewService) {
        this.exitInterviewService = exitInterviewService;
    }

    @PostMapping("/create/{id}")
    public ResponseEntity<List<Questions>> createExitInterviewQuestions(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        List<String> questionTexts = (List<String>) request.get("questionTexts");
        List<Long> selectedStudentIds = null;

        // Check if selectedStudentIds is present and not empty
        if (request.containsKey("selectedStudentIds") && request.get("selectedStudentIds") != null) {
            Object idsObj = request.get("selectedStudentIds");
            if (idsObj instanceof List) {
                List<?> idsList = (List<?>) idsObj;
                if (!idsList.isEmpty()) {
                    selectedStudentIds = idsList.stream()
                            .map(obj -> {
                                if (obj instanceof Number) {
                                    return ((Number) obj).longValue();
                                }
                                return null;
                            })
                            .filter(item -> item != null)
                            .toList();
                }
            }
        }

        List<Questions> questions = exitInterviewService.createMultipleExitInterviewQuestions(
                id,
                questionTexts,
                selectedStudentIds
        );
        return ResponseEntity.ok(questions);
    }


    @GetMapping("/students/all")
    public ResponseEntity<List<StudentDetailsResponse>> getAllStudents() {
        List<StudentDetailsResponse> students = exitInterviewService.getAllStudentsForSelection();  // Updated call
        return ResponseEntity.ok(students);
    }

    @GetMapping("/retrieve-questions/{id}")
    public ResponseEntity<List<Questions>> findAllQuestionByStaff(@PathVariable Long id) {
        List<Questions> staffQuestions = exitInterviewService.findByGuidanceStaffId(id);
        return ResponseEntity.ok(staffQuestions);
    }

    @GetMapping("/student/all-questions")
    public ResponseEntity<List<Questions>> findAllQuestionByStudent() {
        List<Questions> studentQuestions = exitInterviewService.findAllQuestions();
        return ResponseEntity.ok(studentQuestions);
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<ExitInterview> submitAnswer(@RequestBody ExitInterviewRequest request) {
        ExitInterview response = exitInterviewService.studentResponse(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student-response")
    public ResponseEntity<List<ExitInterview>> retrieveStudentResponse() {
        List<ExitInterview> studentResponse = exitInterviewService.retrieveStudentResponse();
        return ResponseEntity.ok(studentResponse);
    }

    @GetMapping("/questions/unanswered")
    public ResponseEntity<List<Questions>> getUnansweredQuestion() {
        List<Questions> unansweredQuestion = exitInterviewService.getUnansweredQuestionsForAuthenticatedStudent();
        return ResponseEntity.ok(unansweredQuestion);
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<Questions> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> request) {

        String questionText = (String) request.get("questionText");
        if (questionText == null) {
            return ResponseEntity.badRequest().build();
        }
        Questions updatedQuestion = exitInterviewService.updateQuestion(questionId, questionText);
        return ResponseEntity.ok(updatedQuestion);
    }
}