package org.rocs.asa.service.appointment.impl;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.exception.domain.AppointmentAlreadyExistException;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
import org.rocs.asa.exception.domain.StudentNotFoundException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.device.token.DeviceTokenRepository;
import org.rocs.asa.repository.notification.NotificationRepository;
import org.rocs.asa.repository.section.SectionRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.appointment.AppointmentService;

import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.rocs.asa.domain.notification.constant.NotificationConstant.TITLE;

/**
 * Service for managing appointment operations between students and guidance staff.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {
    private static Logger LOGGER = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private AppointmentRepository appointmentRepository;
    private StudentRepository studentRepository;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private DeviceTokenRepository deviceTokenRepository;
    private SectionRepository sectionRepository;
    private UserService userService;
    private GuidanceService guidanceService;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  StudentRepository studentRepository,
                                  NotificationService notificationService,
                                  NotificationRepository notificationRepository,
                                  DeviceTokenRepository deviceTokenRepository, SectionRepository sectionRepository, UserService userService, GuidanceService guidanceService) {
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.sectionRepository = sectionRepository;
        this.userService = userService;
        this.guidanceService = guidanceService;
    }

    /**
     * Creates a new appointment with PENDING status.
     *
     * @param request appointment details__
     * @return created appointment data
     */
    @Override
    public Appointment createAppointment(Appointment request) {

        boolean hasAppointment = appointmentRepository.existsByStudentIdAndStatusIn(request.getStudent().getId(),
                List.of("PENDING","SCHEDULED"));

        if(hasAppointment){
            LOGGER.error("Student already have an appointment");
            throw new AppointmentAlreadyExistException("Student already have an appointment");
        }
        LOGGER.info("Request details - Student Number: {}, Scheduled Date: {}, End Date: {}, Notes: {}",
                request.getStudent() != null ? request.getStudent().getStudentNumber() : "NULL",
                request.getScheduledDate(),
                request.getEndDate(),
                request.getNotes());

        LOGGER.info("Step 1: Looking for student with number: {}", request.getStudent().getStudentNumber());
        Student student = studentRepository.findStudentByStudentNumber(request.getStudent().getStudentNumber());

        if(student == null) {
            LOGGER.error("Student not found with number: {}", request.getStudent().getStudentNumber());
            throw new StudentNotFoundException("No student found while creating appointment");
        }

        LOGGER.info("Student found - ID: {}, Name: {} {}",
                student.getId(),
                student.getPerson().getFirstName(),
                student.getPerson().getLastName());

        LOGGER.info("Step 2: Finding logged in guidance staff...");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LOGGER.info("Authentication details:");
        LOGGER.info("  - Principal: {}", auth != null ? auth.getName() : "NULL");
        LOGGER.info("  - Authorities: {}", auth != null ? auth.getAuthorities() : "NULL");
        LOGGER.info("  - Is Authenticated: {}", auth != null ? auth.isAuthenticated() : "false");
        GuidanceStaff guidanceStaff;
        try {
         guidanceStaff = guidanceService.findGuidanceStaff();
            LOGGER.info("Guidance staff found - ID: {}, Name: {} {}",
                    guidanceStaff.getId(),
                    guidanceStaff.getPerson().getFirstName(),
                    guidanceStaff.getPerson().getLastName());
        } catch (Exception e) {
            LOGGER.error(" Failed to find logged in guidance staff: {}", e.getMessage());
            LOGGER.error("Exception type: {}", e.getClass().getSimpleName());
            LOGGER.error("Full stack trace:", e);
            throw e; // Re-throw the original exception
        }

        LOGGER.info("Step 3: Creating new appointment...");
        Appointment newAppointment = new Appointment();
        newAppointment.setStudent(student);
        newAppointment.setGuidanceStaff(guidanceStaff);
        newAppointment.setScheduledDate(request.getScheduledDate());
        newAppointment.setStatus("PENDING");
        newAppointment.setEndDate(request.getEndDate());
        newAppointment.setNotes(request.getNotes());
        newAppointment.setDateCreated(LocalDateTime.now());
        newAppointment.setAppointmentType(request.getAppointmentType());

        LOGGER.info("Appointment created with status: {}", newAppointment.getStatus());
        Appointment saveAppointment;

        try {
            saveAppointment = appointmentRepository.save(newAppointment);
            LOGGER.info(" Appointment saved successfully with ID: {}", saveAppointment.getAppointmentId());
        } catch (Exception e) {
            LOGGER.error("Failed to save appointment: {}", e.getMessage());
            LOGGER.error(" Database error:", e);
            throw e;
        }

        validateScheduledDateEndDate(newAppointment.getScheduledDate(),newAppointment.getEndDate());

        String targetUserId = student.getUser().getUserId();
        LOGGER.info("Target user ID for notification: {}", targetUserId);


        LOGGER.info("Notification details:");
        LOGGER.info("  - Title: {}", TITLE);
        LOGGER.info("  - Message: {}", notificationMessageForStudent(guidanceStaff,student,request));
        LOGGER.info("  - Type: APPOINTMENT_REQUEST");

        try {
            notificationService.sendNotificationToUser(targetUserId,
                    TITLE,
                    notificationMessageForStudent(guidanceStaff,student,request),
                    "APPOINTMENT_REQUEST"
            );
            LOGGER.info(" Notification sent successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to send notification: {}", e.getMessage());
            LOGGER.error(" Notification error:", e);
        }

       notificationService.saveNotification(student.getUser(), saveAppointment,
               notificationMessageForStudent(guidanceStaff,student,request), "APPOINTMENT_REQUEST"
       );
        LOGGER.info("Notification save successfully");

        LOGGER.info(" Appointment created successfully with appointmentId = {}", saveAppointment.getAppointmentId());

        return saveAppointment;
    }

    @Override
    public Appointment studentResponseToAppointment(Long appointmentId, Map<String, String> data) {
        LOGGER.info("Student responding to appointment ID {}", appointmentId);

        Appointment appointment = findAppointmentsByAppointmentId(appointmentId);

        if (!appointment.getStudent().getUser().getUserId().equals(appointment.getStudent().getUser().getUserId())) {
            throw new IllegalArgumentException("Unauthorized: You are not assigned to this appointment");
        }

        if (!"PENDING".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Appointment is not in PENDING status");
        }

        LOGGER.info("status: " + appointment.getStatus());

        String action = data.get("action");
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action is required");
        }

        switch (action.toUpperCase()) {
            case "ACCEPT":
                appointment.setStatus("SCHEDULED");
                break;
            case "DECLINE":
                appointment.setStatus("CANCELED");
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }

        String targetUser = appointment.getGuidanceStaff().getUser().getUserId();
        String notificationMessage = notificationMessageResponseForGuidance(
                appointment.getStudent(),
                action,
                appointment
        );

        notificationService.sendNotificationToUser(
                targetUser,
                "Appointment Response",
                notificationMessage,
                action.toUpperCase()
        );

        appointmentRepository.save(appointment);

        Notifications notifications = new Notifications();
        notifications.setUser(appointment.getGuidanceStaff().getUser());
        notifications.setAppointment(appointment);
        notifications.setMessage(
                appointment.getStudent().getPerson().getFirstName() + " " +
                        appointment.getStudent().getPerson().getLastName() +
                        " has " + action.toUpperCase() + "ed your appointment request"
        );
        notifications.setActionType(action.toUpperCase());
        notifications.setCreatedAt(LocalDateTime.now());
        notifications.setIsRead(1);

        notificationRepository.save(notifications);

        return appointment;
    }

    @Override
    public List<Appointment> getAppointmentByGuidanceStaff(Long employeeNumber) {
        return appointmentRepository.findByGuidanceStaff_Id(employeeNumber);
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

            Appointment appointment = appointmentRepository.findById(appointmentId)
               .orElseThrow(() -> new AppointmentNotFoundException("Appointment Not Found"));

            LOGGER.info("Successfully found appointment with ID: {}", appointmentId);
            return appointment;
    }

    @Override
    public List<Appointment> findAppointmentByStatus(Long guidanceStaffId ,String status) {
        LOGGER.info("Finding appointments with status: {}", status);
        return appointmentRepository.findByGuidanceStaff_IdAndStatusIgnoreCase(guidanceStaffId,status);

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
    private void markAsOnGoingOrIsCompleted() {
        LocalDateTime now = LocalDateTime.now();

        List<Appointment> appointments = appointmentRepository.findByStatusIn(List.of("SCHEDULED","ONGOING"));

        for(Appointment appointment : appointments) {
            if (now.isAfter(appointment.getEndDate())) {
                appointment.setStatus("COMPLETED");

            }else if (now.isAfter(appointment.getScheduledDate()) && now.isBefore(appointment.getEndDate())){
                appointment.setStatus("ONGOING");
            }
            appointmentRepository.save(appointment);
        }
    }

    private String notificationMessageForStudent(GuidanceStaff guidanceStaff, Student student, Appointment appointment) {
        String counselorName =  guidanceStaff.getPerson().getFirstName().toUpperCase() +
                guidanceStaff.getPerson().getLastName().toUpperCase();


        String scheduledDate = formatDateTime(appointment.getScheduledDate());
        String endDate = formatTime(appointment.getEndDate());

        return String.format(
                "Appointment with %s on %s (%s - %s)",
                counselorName,
                scheduledDate.split(" at ")[0],
                scheduledDate.split(" at ")[1],
                endDate
        );
    }

    private String notificationMessageResponseForGuidance(Student student, String action, Appointment appointment) {
        return student.getPerson().getFirstName() + " " +
                student.getPerson().getLastName() +
                " has " + action.toLowerCase() +
                " your appointment request scheduled at " +
                appointment.getScheduledDate() +
                " until " + appointment.getEndDate();
    }





    private void validateScheduledDateEndDate(LocalDateTime scheduledDate, LocalDateTime endDate) {
        if (scheduledDate == null || endDate == null) {
            throw new IllegalArgumentException("Scheduled date and end date cannot be null");
        }

        if (scheduledDate.isAfter(endDate) || scheduledDate.isEqual(endDate)) {
            throw new IllegalArgumentException(
                    "Scheduled date must be before end date. " +
                            "Scheduled: " + scheduledDate + ", End: " + endDate
            );
        }

        LocalDateTime now = LocalDateTime.now();
        if (scheduledDate.isBefore(now)) {
            throw new IllegalArgumentException("Scheduled date cannot be in the past");
        }
        if (endDate.isBefore(now)) {
            throw new IllegalArgumentException("End date cannot be in the past");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");
        return dateTime.format(formatter);
    }
    private String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return dateTime.format(formatter);
    }



}
