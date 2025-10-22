package org.rocs.asa.controller.student;

import jakarta.validation.Valid;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.dto.StudentInformationDto;
import org.rocs.asa.service.student.profile.impl.StudentProfileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Period;

/**
 * REST controller for managing student profiles.
 * Provides endpoints to retrieve and create students.
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    private StudentProfileServiceImpl studentService;
    /**
     * Constructs the StudentController with the given StudentProfileServiceImpl.
     *
     * @param studentService the service handling student profile operations
     */
    @Autowired
    public StudentController(StudentProfileServiceImpl studentService) {
        this.studentService = studentService;
    }

    /**
     * Saves a new student profile.
     *
     * @param student the Student object to be saved
     * @return ResponseEntity containing the newly created Student object and HTTP status 200 OK
     */
    @PostMapping("/save/student-profile")
    public ResponseEntity<Student> createStudentProfile(@Valid @RequestBody Student student){
       Student newStudent = studentService.saveStudentProfile(student);
       return new ResponseEntity<>(newStudent,HttpStatus.OK);
    }

    @GetMapping("/findBy/{studentNumber}")
    public ResponseEntity<StudentInformationDto> getStudentByStudentNumber (@PathVariable String studentNumber ) {
        StudentInformationDto studentInformation = studentService.getPersonByStudentNumber(studentNumber);
       return new ResponseEntity<>(studentInformation,HttpStatus.OK);
    }
}
