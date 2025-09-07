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

@RestController
@RequestMapping("/counselor")
@CrossOrigin("*")

public class AppointmentController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppointmentController.class);
    private AppointmentService appointmentService;


    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/retrieve-appointment")
    public ResponseEntity<List<Appointment>> getAllAppointment(){
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
    @PostMapping("/create-appointment")
    public ResponseEntity<AppointmentResponseDto> createAppointment (@Valid @RequestBody CreateAppointmentRequestDto requestDto){
        AppointmentResponseDto appointment = appointmentService.createAppointment(requestDto);
        return ResponseEntity.ok(appointment);
    }
    @GetMapping("/appointment/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentFindByStatus(@PathVariable String status){
        List<Appointment> allAppointments = appointmentService.findAppointmentByStatus(status);
        return ResponseEntity.ok(allAppointments);
    }
    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getAppointmentByAppointmentId(@PathVariable Long appointmentId){
         Appointment appointment = appointmentService.findAppointmentsByAppointmentId(appointmentId);
          return ResponseEntity.ok(appointment);
    }
}
