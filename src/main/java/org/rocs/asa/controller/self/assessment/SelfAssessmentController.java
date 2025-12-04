package org.rocs.asa.controller.self.assessment;

import org.apache.coyote.Response;
import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.self.assesment.SelfAssessment;
import org.rocs.asa.domain.self.request.SelfAssessmentRequest;
import org.rocs.asa.service.questions.impl.QuestionsServiceImpl;
import org.rocs.asa.service.self.assessment.SelfAssesmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/self-assessment")
public class SelfAssessmentController {
    private SelfAssesmentService assesmentService;
    private QuestionsServiceImpl questionsService;


    @Autowired
    public SelfAssessmentController(SelfAssesmentService assesmentService,QuestionsServiceImpl questionsService) {
        this.assesmentService = assesmentService;
        this.questionsService = questionsService;
    }

    @PostMapping("/create/{id}")
    public ResponseEntity<List<Questions>> createCreateQuestions(@PathVariable Long id, @RequestBody List<String> questionText) {
       List<Questions> questions = assesmentService.createMultipleSelfAssessmentQuestions(id,questionText);
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
