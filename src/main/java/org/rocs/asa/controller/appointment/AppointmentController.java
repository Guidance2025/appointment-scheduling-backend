package org.rocs.asa.controller.appointment;

import jakarta.validation.Valid;
import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.response.BookedSlotsResponse;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.service.appointment.AppointmentService;
import org.rocs.asa.service.guidance.GuidanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {@code AppointmentController} handles all appointment-related operations
 */
@RestController
@RequestMapping("/counselor")
@CrossOrigin("*")
public class AppointmentController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppointmentController.class);
    private AppointmentService appointmentService;
    private GuidanceService guidanceService;

    /**
     * Constructs a new {@code AppointmentController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     * @param appointmentService the service layer for managing appointment operations
     */
    @Autowired
    public AppointmentController(AppointmentService appointmentService, GuidanceService guidanceService) {
        this.appointmentService = appointmentService;
        this.guidanceService = guidanceService;
    }

    /**
     * {@code getAllAppointment} used to retrieve all appointments from the system
     * @return ResponseEntity containing list of all appointments, and Http Status
     */
    @GetMapping("/retrieve-appointment")
    public ResponseEntity<List<Appointment>> getAllAppointment() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * {@code createAppointment} used to create a new appointment in the system
     * @param request that contains the appointment details
     * @return ResponseEntity containing the created appointment object, and Http Status
     */
    @PostMapping("/create-appointment")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment request) {
        Appointment appointment = appointmentService.createAppointment(request);
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    /**
     * {@code getAppointmentFindByStatus} used to retrieve appointments filtered by status and guidance ID
     * @param status that indicates the appointment status to filter
     * @param guidanceId that identifies the guidance staff
     * @return ResponseEntity containing list of appointments matching the criteria, and Http Status
     */
    @GetMapping("/appointment/{status}/{guidanceId}")
    public ResponseEntity<List<Appointment>> getAppointmentFindByStatus(@PathVariable String status, @PathVariable Long guidanceId) {
        List<Appointment> allAppointments = appointmentService.findAppointmentByStatus(guidanceId, status);
        return ResponseEntity.ok(allAppointments);
    }

    /**
     * {@code getAppointmentByAppointmentId} used to retrieve a specific appointment by its ID
     * @param appointmentId that identifies the appointment
     * @return ResponseEntity containing the appointment object, and Http Status
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getAppointmentByAppointmentId(@PathVariable Long appointmentId) {
        Appointment appointment = appointmentService.findAppointmentsByAppointmentId(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * {@code getResponseAppointment} used to handle student response to an appointment
     * @param appointmentId that identifies the appointment
     * @param data that contains the student's response information
     * @return ResponseEntity containing the updated appointment object, and Http Status
     */
    @PostMapping("/{appointmentId}/response")
    public ResponseEntity<Appointment> getResponseAppointment(@PathVariable Long appointmentId, @RequestBody Map<String, String> data) {
        Appointment response = appointmentService.studentResponseToAppointment(appointmentId, data);
        return ResponseEntity.ok(response);
    }

    /**
     * {@code getAppointmentByGuidanceStaff} used to retrieve all appointments for a specific guidance staff member
     * @param employeeNumber that identifies the guidance staff employee
     * @return ResponseEntity containing list of appointments assigned to the guidance staff, and Http Status
     */
    @GetMapping("/find/appointment/{employeeNumber}")
    public ResponseEntity<List<Appointment>> getAppointmentByGuidanceStaff(@PathVariable Long employeeNumber) {
        List<Appointment> guidanceStaffAppointment = appointmentService.getAppointmentByGuidanceStaff(employeeNumber);
        return ResponseEntity.ok(guidanceStaffAppointment);
    }
    @GetMapping("/booked-slots")
    public ResponseEntity<List<BookedSlotsResponse>> getBookedSlots(@RequestParam String date) {
            List<BookedSlotsResponse> bookedSlotResponses = appointmentService.getBookedSlots(date);
            return ResponseEntity.ok(bookedSlotResponses);
    }
    @GetMapping("/all")
    ResponseEntity<List<GuidanceStaff>> findAuthenticatedGuidanceStaff() {
        List<GuidanceStaff> guidanceStaff = guidanceService.findActiveGuidanceStaff();
        return ResponseEntity.ok(guidanceStaff);
    }
    @PostMapping("/{appointmentId}/guidance/response")
    public ResponseEntity<Appointment> guidanceResponseToAppointmentRequest(@PathVariable Long appointmentId, @RequestBody Map<String,String> action) {
        Appointment response = appointmentService.guidanceStaffResponse(appointmentId,action);
        return ResponseEntity.ok(response);
    }
}