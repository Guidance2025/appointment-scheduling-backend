package org.rocs.asa.controller.student;

import jakarta.validation.Valid;
import org.rocs.asa.domain.student.information.response.StudentInformationDto;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.service.student.profile.impl.StudentProfileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * {@code StudentController} handles all student profile operations
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    private StudentProfileServiceImpl studentService;

    /**
     * Constructs a new {@code StudentController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     * @param studentService the service layer for managing student operations
     */
    @Autowired
    public StudentController(StudentProfileServiceImpl studentService) {
        this.studentService = studentService;
    }

    /**
     * {@code createStudentProfile} used to create and save a new student profile
     * @param student that contains the student information to be saved
     * @return ResponseEntity containing the newly created student object, and Http Status
     */
    @PostMapping("/save/student-profile")
    public ResponseEntity<Student> createStudentProfile(@Valid @RequestBody Student student) {
        Student newStudent = studentService.saveStudentProfile(student);
        return new ResponseEntity<>(newStudent, HttpStatus.OK);
    }

    /**
     * {@code getStudentByStudentNumber} used to retrieve student information by student number
     * @param studentNumber that identifies the student
     * @return ResponseEntity containing the student information details, and Http Status
     */
    @GetMapping("/findBy/{studentNumber}")
    public ResponseEntity<StudentInformationDto> getStudentByStudentNumber(@PathVariable String studentNumber) {
        StudentInformationDto studentInformation = studentService.getPersonByStudentNumber(studentNumber);
        return new ResponseEntity<>(studentInformation, HttpStatus.OK);
    }
}