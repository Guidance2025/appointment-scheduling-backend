package org.rocs.asa.controller.student;

import org.rocs.asa.domain.person.student.Student;
import org.rocs.asa.service.student.impl.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
public class StudentController {

    private StudentServiceImpl studentService;

    @Autowired
    public StudentController(StudentServiceImpl studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/findBy/{studentNumber}")
    public ResponseEntity<Student> findStudentByStudentNumber(@PathVariable String studentNumber){
         Student student = studentService.findStudentByStudentNumber(studentNumber);
                return new ResponseEntity<>(student,HttpStatus.OK);
    }


}
