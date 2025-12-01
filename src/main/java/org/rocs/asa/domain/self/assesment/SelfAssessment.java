//package org.rocs.asa.domain.self.assesment;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.rocs.asa.domain.questions.Questions;
//import org.rocs.asa.domain.student.Student;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@Table(name = "tbl_self_assessment")
//public class SelfAssessment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "assessment_response_id")
//    private Long id;
//
//    @JoinColumn(name = "student_id")
//    private Student student;
//
//    @JoinColumn(name = "questions_id")
//    private List<Questions> questions;
//
//    private String responseText;
//
//    private LocalDateTime responseDate;
//
//
//}
