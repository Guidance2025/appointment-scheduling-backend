package org.rocs.asa.controller.self.assessment;

import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.self.assesment.SelfAssessment;
import org.rocs.asa.domain.self.request.SelfAssessmentRequest;
import org.rocs.asa.service.self.assessment.SelfAssesmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/self-assessment")
public class SelfAssessmentController {
    private SelfAssesmentService assesmentService;

    @Autowired
    public SelfAssessmentController(SelfAssesmentService assesmentService) {
        this.assesmentService = assesmentService;
    }

    @PostMapping("/create/{id}")
    public ResponseEntity<List<Questions>> createCreateQuestions(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        List<String> questionTexts = (List<String>) request.get("questionTexts");
        List<Questions> questions = assesmentService.createMultipleSelfAssessmentQuestions(id, questionTexts);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/retrieve-questions/{id}")
    public ResponseEntity<List<Questions>> findAllQuestionByStaff(@PathVariable Long id) {
        List<Questions> staffQuestions = assesmentService.findByGuidanceStaffId(id);
        return ResponseEntity.ok(staffQuestions);
    }

    @GetMapping("/student/all-questions")
    public ResponseEntity<List<Questions>> findAllQuestionByStudent() {
        List<Questions> studentQuestions = assesmentService.findAllQuestions();
        return ResponseEntity.ok(studentQuestions);
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<SelfAssessment> submitAnswer(@RequestBody SelfAssessmentRequest request) {
        SelfAssessment response = assesmentService.studentResponse(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student-response")
    public ResponseEntity<List<SelfAssessment>> retrieveStudentResponse(){
        List <SelfAssessment> studentResponse = assesmentService.retrieveStudentResponse();
        return ResponseEntity.ok(studentResponse);
    }

    @GetMapping("/questions/unanswered")
    public ResponseEntity<List<Questions>> getUnansweredQuestion() {
        List<Questions> unansweredQuestion = assesmentService.getUnansweredQuestionsForAuthenticatedStudent();
        return ResponseEntity.ok(unansweredQuestion);
    }
}