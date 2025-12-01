package org.rocs.asa.controller.student;

import jakarta.validation.Valid;
import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.student.information.response.StudentInfoResponse;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.student.request.UpdateStudentProfileRequest;
import org.rocs.asa.service.appointment.AppointmentService;
import org.rocs.asa.service.student.profile.impl.StudentProfileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code StudentController} handles all student profile operations
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    private StudentProfileServiceImpl studentService;
    private AppointmentService appointmentService;

    /**
     * Constructs a new {@code StudentController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     * @param studentService the service layer for managing student operations
     */
    @Autowired
    public StudentController(StudentProfileServiceImpl studentService, AppointmentService appointmentService) {
        this.studentService = studentService;
        this.appointmentService = appointmentService;
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
    public ResponseEntity<StudentInfoResponse> getStudentByStudentNumber(@PathVariable String studentNumber) {
        StudentInfoResponse studentInformation = studentService.getPersonByStudentNumber(studentNumber);
        return new ResponseEntity<>(studentInformation, HttpStatus.OK);
    }
    @GetMapping("/appointment/{studentId}/{status}")
    public ResponseEntity<List<Appointment>> getStudentAppointment (@PathVariable Long studentId, @PathVariable String status) {
        List<Appointment> studentAppointments = appointmentService.findStudentAppointments(studentId,status);
        return ResponseEntity.ok(studentAppointments);
    }

    @PostMapping("/create-appointment")
    public ResponseEntity<Appointment> studentSetAppointment (@RequestBody Appointment appointment) {
        Appointment setAppointment = appointmentService.studentCreateAppointment(appointment);
        return new ResponseEntity<>(setAppointment, HttpStatus.OK);
    }

    @GetMapping("/retrieve/profile/{id}")
    public ResponseEntity<Student> getStudentProfile(@PathVariable Long id) {
        Student student = studentService.getStudentProfile(id);
        return new ResponseEntity<>(student,HttpStatus.OK);
    }
    @GetMapping("/appointment/{id}")
    public ResponseEntity<List<Appointment>> getAppointmentByStudentId (@PathVariable Long id) {
        List<Appointment> appointments = appointmentService.getAppointmentByStudent(id);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Student> updateStudentProfile(@PathVariable Long id, @RequestBody UpdateStudentProfileRequest request) {
        Student updatedStudent = studentService.updateStudentProfile(id, request);
        return ResponseEntity.ok(updatedStudent);
    }
}