package org.rocs.asa.controller.exit.interview;

import org.rocs.asa.dto.exit.ExitInterviewDetailDto;
import org.rocs.asa.dto.exit.StudentListRow;
import org.rocs.asa.dto.exit.SubmitAnswerRequest;
import org.rocs.asa.service.exit.ExitInterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exit-interview")
public class ExitInterviewController {

    private final ExitInterviewService service;

    public ExitInterviewController(ExitInterviewService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentListRow>> students(
            @RequestParam(defaultValue = "All") String course,
            @RequestParam(defaultValue = "All") String cluster) {
        return ResponseEntity.ok(service.getStudents(course, cluster));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ExitInterviewDetailDto> studentDetail(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentDetail(studentId));
    }

    @PostMapping("/student/{studentId}")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long studentId,
            @RequestBody SubmitAnswerRequest req) {
        service.saveAnswer(studentId, req.getQuestionId(), req.getResponseText());
        return ResponseEntity.ok().build();
    }
}