package org.rocs.asa.service.appointment.impl;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.dto.appointment.create.appointment.request.CreateAppointmentRequestDto;
import org.rocs.asa.dto.appointment.create.appointment.response.AppointmentResponseDto;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.exception.domain.StudentNotFoundException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.appointment.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Service for managing appointment operations between students and guidance staff.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {
    private static Logger LOGGER = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private AppointmentRepository appointmentRepository;
    private StudentRepository studentRepository;
    private GuidanceStaffRepository guidanceStaffRepository;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, StudentRepository studentRepository, GuidanceStaffRepository guidanceStaffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
    }
    /**
     * Creates a new appointment with PENDING status.
     *
     * @param dto appointment details
     * @return created appointment data
     */
    @Override
    public AppointmentResponseDto createAppointment(CreateAppointmentRequestDto dto) {

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(dto.getEmployeeNumber())
                .orElseThrow(() -> new GuidanceStaffNotFoundException("Guidance Staff not found"));

        Appointment appointment = new Appointment();
        appointment.setStudent(student);
        appointment.setGuidanceStaff(guidanceStaff);
        appointment.setScheduledDate(dto.getScheduledDate());
        appointment.setAppointmentType(dto.getAppointmentType());
        appointment.setStatus("PENDING");
        appointment.setNotes(dto.getNotes());
        appointment.setDateCreated(LocalDateTime.now());

        Appointment saveAppointment = appointmentRepository.save(appointment);

        LOGGER.info("Appointment created successfully with appointmentId ={}", saveAppointment.getAppointmentId());
        return new AppointmentResponseDto(saveAppointment);
    }
    /**
     * Looks up a single appointment using its ID number.
     * Makes sure the ID isn't empty, then searches the database for that specific appointment.
     * Includes proper error handling so you know exactly what went wrong if something fails.
     *
     * @param appointmentId the unique ID number of the appointment you want to find
     * @return the complete appointment record with all its details
     * @throws IllegalArgumentException when the ID is null or missing
     * @throws AppointmentNotFoundException when no appointment exists with that ID
     */
    @Override
    public Appointment findAppointmentsByAppointmentId(Long appointmentId) {
        LOGGER.info("Searching for Appointment with AppointmentId {}", appointmentId );

        if (appointmentId == null) {
            LOGGER.error("Appointment ID cannot be null");
            throw new AppointmentNotFoundException("Appointment ID cannot be null");
        }
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> {

                LOGGER.error("Appointment not found with ID: {}", appointmentId);
                return new AppointmentNotFoundException(
                        "Appointment not found with ID: " + appointmentId);
            });

            LOGGER.info("Successfully found appointment with ID: {}", appointmentId);
            return appointment;


        } catch (Exception e) {
            if (e instanceof AppointmentNotFoundException) {
                throw e;
            }
            LOGGER.error("Unexpected error while finding appointment with ID: {}", appointmentId, e);
            throw new RuntimeException("Error retrieving appointment", e);
        }
    }

    @Override
    public List<Appointment> findAppointmentByStatus(String status) {
        LOGGER.info("Finding appointments with status: {}", status);

        if (status == null || status.trim().isEmpty()) {
            LOGGER.error("Status cannot be null or empty");
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        try {
            List<Appointment> appointments = appointmentRepository.findByStatusIgnoreCase(status.trim());
            LOGGER.info("Found {} appointments with status: {}", appointments.size(), status);
            return appointments;

        } catch (AppointmentNotFoundException e) {
            LOGGER.error("Error finding appointments with status: {}", status, e);
            throw new AppointmentNotFoundException("Error retrieving appointments by status");
        }
    }

    @Override
    public List<Appointment> getAllAppointments() {
        LOGGER.info("Retrieving all appointments");
        try {
            List<Appointment> appointments = appointmentRepository.findAll();
            LOGGER.info("Successfully retrieved {} appointments", appointments.size());
            return appointments;

        } catch (AppointmentNotFoundException e) {
            LOGGER.error("Error while retrieving all appointments", e);
            throw new AppointmentNotFoundException("Error retrieving all appointments");
        }
    }
}
