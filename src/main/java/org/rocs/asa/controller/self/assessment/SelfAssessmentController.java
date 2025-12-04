//package org.rocs.asa.controller.self.assessment;
//
//import org.rocs.asa.domain.questions.Questions;
//import org.rocs.asa.service.questions.impl.QuestionsServiceImpl;
//import org.rocs.asa.service.self.assessment.SelfAssesmentService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/self-assessment")
//public class SelfAssessmentController {
//    private SelfAssesmentService service;
//    private QuestionsServiceImpl questionsService;
//
//
//    @Autowired
//    public SelfAssessmentController(SelfAssesmentService service,QuestionsServiceImpl questionsService) {
//        this.service = service;
//        this.questionsService = questionsService;
//    }
//
//    @GetMapping("/create/{id}")
//    public ResponseEntity<List<Questions>> createCreateQuestions(@PathVariable Long id, @RequestBody List<String> questionText) {
//       List<Questions> questions = questionsService.createMultipleSelfAssessmentQuestions(id,questionText);
//        return ResponseEntity.ok(questions);
//    }
//
//}
