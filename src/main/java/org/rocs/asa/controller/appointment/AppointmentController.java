package org.rocs.asa.controller.appointment;

import jakarta.validation.Valid;
import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.dto.appointment.create.appointment.request.CreateAppointmentRequestDto;
import org.rocs.asa.dto.appointment.create.appointment.response.AppointmentResponseDto;
import org.rocs.asa.service.appointment.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * REST controller for managing appointments.
 * Provides endpoints for managing appointments.
 */
@RestController
@RequestMapping("/counselor")
@CrossOrigin("*")

public class AppointmentController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppointmentController.class);
    private AppointmentService appointmentService;

    /**
     * Constructs the AppointmentController with the given AppointmentService.
     *
     * @param appointmentService the service handling appointment operations
     */
    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    /**
     * Retrieves all appointments.
     *
     * @return ResponseEntity containing a list of all Appointment objects and HTTP status 200 OK
     */
    @GetMapping("/retrieve-appointment")
    public ResponseEntity<List<Appointment>> getAllAppointment() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
    /**
     * Creates a new appointment.
     *
     * @param requestDto the DTO containing appointment creation details
     * @return ResponseEntity containing the created AppointmentResponseDto and HTTP status 200 OK
     */
    @PostMapping("/create-appointment")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment requestDto) {
        Appointment appointment = appointmentService.createAppointment(requestDto);
        return new ResponseEntity<>(appointment,HttpStatus.OK);
    }
    /**
     * Retrieves appointments filtered by status.
     *
     * @param status the status to filter appointments
     * @return ResponseEntity containing a list of Appointment objects matching the status and HTTP status 200 OK
     */
    @GetMapping("/appointment/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentFindByStatus(@PathVariable String status) {
        List<Appointment> allAppointments = appointmentService.findAppointmentByStatus(status);
        return ResponseEntity.ok(allAppointments);
    }
    /**
     * Retrieves an appointment by its ID.
     *
     * @param appointmentId the unique ID of the appointment
     * @return ResponseEntity containing the Appointment object and HTTP status 200 OK
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getAppointmentByAppointmentId(@PathVariable Long appointmentId) {
        Appointment appointment = appointmentService.findAppointmentsByAppointmentId(appointmentId);
        return ResponseEntity.ok(appointment);
    }
}