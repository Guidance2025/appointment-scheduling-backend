package org.rocs.asa.service.appointment.impl;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.response.BookedSlotsResponse;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.exception.domain.*;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.notification.NotificationRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.appointment.AppointmentService;
import org.rocs.asa.service.appointment.helper.AppointmentMessageBuilder;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.student.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Value("${appointment.max-pending-per-student:3}")
    private int maxPendingAppointments;

    private static final int REMINDER_MINUTES_BEFORE = 25;
    private static final int REMINDER_WINDOW_MINUTES = 5;

    private final AppointmentRepository appointmentRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final GuidanceService guidanceService;
    private final StudentService studentService;
    private final GuidanceStaffRepository guidanceStaffRepository;

    @Autowired
    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            StudentRepository studentRepository,
            NotificationService notificationService,
            NotificationRepository notificationRepository,
            GuidanceService guidanceService,
            StudentService studentService,
            GuidanceStaffRepository guidanceStaffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.guidanceService = guidanceService;
        this.studentService = studentService;
        this.guidanceStaffRepository = guidanceStaffRepository;
    }

    /**
     * Creates an appointment for a student by the authenticated guidance staff.
     *
     * @param request the appointment details including student, scheduled dates, and notes
     * @return the created appointment with PENDING status
     * @throws StudentNotFoundException if the student is not found
     * @throws AppointmentAlreadyExistException if time conflicts exist
     */
    @Override
    @Transactional
    public Appointment createAppointment(Appointment request) {
        LOGGER.info("Guidance staff creating appointment for student");

        validateAppointmentDates(request.getScheduledDate(), request.getEndDate());

        Student student = findStudentByNumber(request.getStudent().getStudentNumber());
        GuidanceStaff guidanceStaff = guidanceService.findAuthenticatedGuidanceStaff();

        validateAppointmentAvailability(student.getId(), guidanceStaff.getId(),
                request.getScheduledDate(), request.getEndDate());

        Appointment savedAppointment = createAndSaveAppointment(student, guidanceStaff, request);

        notifyStudent(savedAppointment);

        LOGGER.info("Appointment created successfully. ID: {}", savedAppointment.getAppointmentId());
        return savedAppointment;
    }

    /**
     * Creates an appointment initiated by the authenticated student.
     *
     * @param request the appointment details including guidance staff selection and time
     * @return the created appointment with PENDING status
     * @throws StudentNotFoundException if student is not authenticated or found
     * @throws GuidanceStaffNotFoundException if the selected guidance staff is not found
     * @throws RateLimitExceededException if student has too many pending appointments
     * @throws AppointmentAlreadyExistException if time conflicts exist
     */
    @Override
    @Transactional
    public Appointment studentCreateAppointment(Appointment request) {
        LOGGER.info("Student creating appointment");

        validateAppointmentDates(request.getScheduledDate(), request.getEndDate());

        Student student = studentService.findByAuthenticatedStudent();
        if (student == null) {
            throw new StudentNotFoundException("Student not found");
        }

        validatePendingAppointmentLimit(student.getId());

        GuidanceStaff guidanceStaff = findGuidanceStaffById(request.getGuidanceStaff().getId());

        validateAppointmentAvailability(student.getId(), guidanceStaff.getId(),
                request.getScheduledDate(), request.getEndDate());

        Appointment savedAppointment = createAndSaveAppointment(student, guidanceStaff, request);

        notifyGuidanceStaff(savedAppointment);

        LOGGER.info("Student appointment created successfully. ID: {}", savedAppointment.getAppointmentId());
        return savedAppointment;
    }

    /**
     * Handles guidance staff response to a pending appointment request.
     *
     * @param appointmentId the ID of the appointment to respond to
     * @param data map containing the action ("accept" or "decline")
     * @return the updated appointment with new status
     * @throws AppointmentNotFoundException if appointment is not found
     * @throws IllegalArgumentException if appointment is not pending or action is invalid
     */
    @Override
    @Transactional
    public Appointment guidanceStaffResponse(Long appointmentId, Map<String, String> data) {
        Appointment appointment = findAppointmentById(appointmentId);

        validateAppointmentIsPending(appointment);

        String action = extractAndValidateAction(data);
        updateAppointmentStatus(appointment, action);

        appointmentRepository.save(appointment);

        notifyUserOfResponse(appointment, action, false);

        LOGGER.info("Guidance staff {} appointment ID: {}", action, appointmentId);
        return appointment;
    }

    /**
     * Handles student response to a pending appointment request.
     *
     * @param appointmentId the ID of the appointment to respond to
     * @param data map containing the action ("accept" or "decline")
     * @return the updated appointment with new status
     * @throws AppointmentNotFoundException if appointment is not found
     * @throws IllegalArgumentException if appointment is not pending or action is invalid
     */
    @Override
    @Transactional
    public Appointment studentResponseToAppointment(Long appointmentId, Map<String, String> data) {
        Appointment appointment = findAppointmentById(appointmentId);

        validateAppointmentIsPending(appointment);

        String action = extractAndValidateAction(data);
        updateAppointmentStatus(appointment, action);

        appointmentRepository.save(appointment);

        notifyUserOfResponse(appointment, action, true);

        LOGGER.info("Student {} appointment ID: {}", action, appointmentId);
        return appointment;
    }

    /**
     * Updates appointment statuses to ONGOING or COMPLETED based on current time.
     * This method called by a scheduled job.
     */
    /**
     * Updates appointment statuses to ONGOING or COMPLETED based on current time.
     * This method is called by a scheduled job.
     */
    @Override
    @Transactional
    public void markAsOnGoingOrIsCompleted() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Manila"));

        List<Appointment> appointments = appointmentRepository.findByStatusInOptimized(
                List.of(
                        AppointmentStatus.SCHEDULED.name(),
                        AppointmentStatus.ONGOING.name()
                )
        );

        List<Appointment> toUpdate = new ArrayList<>();

        for (Appointment appointment : appointments) {

            LocalDateTime start = toPH(appointment.getScheduledDate());
            LocalDateTime end   = toPH(appointment.getEndDate());

            String oldStatus = appointment.getStatus();
            String newStatus = determineAppointmentStatus(start, end, now, oldStatus);

            if (!oldStatus.equals(newStatus)) {
                appointment.setStatus(newStatus);
                toUpdate.add(appointment);
            }
        }

        if (!toUpdate.isEmpty()) {
            appointmentRepository.saveAll(toUpdate);
            LOGGER.info("Updated {} appointment statuses", toUpdate.size());
        }
    }


    /**
     * Sends reminder notifications to students 30 minutes before their appointments.
     */
    @Override
    @Transactional
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderStart = now.plusMinutes(REMINDER_MINUTES_BEFORE - REMINDER_WINDOW_MINUTES);
        LocalDateTime reminderEnd = now.plusMinutes(REMINDER_MINUTES_BEFORE + REMINDER_WINDOW_MINUTES);

        List<Appointment> upcomingAppointments = appointmentRepository
                .findByStatusAndScheduledDateBetween(
                        AppointmentStatus.SCHEDULED.name(),
                        reminderStart,
                        reminderEnd
                );

        if (upcomingAppointments.isEmpty()) {
            LOGGER.debug("No upcoming appointments found for reminders");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (Appointment appointment : upcomingAppointments) {
            try {
                if (shouldSendReminder(appointment)) {
                    sendReminderToStudent(appointment);
                    successCount++;
                    LOGGER.info("Reminder sent for appointment ID: {}", appointment.getAppointmentId());
                }
            } catch (Exception e) {
                failureCount++;
                LOGGER.error("Failed to send reminder for appointment ID: {}. Error: {}",
                        appointment.getAppointmentId(), e.getMessage());
            }
        }

        LOGGER.info("Appointment reminders: {} sent, {} failed", successCount, failureCount);
    }

    private boolean shouldSendReminder(Appointment appointment) {
        List<Notifications> existingReminders = notificationRepository
                .findByAppointment_AppointmentIdAndActionType(
                        appointment.getAppointmentId(),
                        "APPOINTMENT_REMINDER"
                );

        return existingReminders.isEmpty();
    }

    private void sendReminderToStudent(Appointment appointment) {
        Student student = appointment.getStudent();
        GuidanceStaff guidanceStaff = appointment.getGuidanceStaff();

        String message = buildReminderMessage(appointment, guidanceStaff);
        String title = "Appointment Reminder";

        notificationService.sendNotificationToUser(
                student.getUser().getUserId(),
                title,
                message,
                "APPOINTMENT_REMINDER"
        );

        notificationService.saveNotification(
                student.getUser(),
                appointment,
                message,
                "APPOINTMENT_REMINDER"
        );
    }

    private String buildReminderMessage(Appointment appointment, GuidanceStaff guidanceStaff) {
        String staffName = getGuidanceStaffFullName(guidanceStaff);

        ZonedDateTime manilaTime = appointment.getScheduledDate()
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Manila"));

        String appointmentTime = manilaTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        String appointmentDate = manilaTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        return String.format(
                "Reminder: Your appointment with %s is in 30 minutes at %s on %s. Type: %s",
                staffName,
                appointmentTime,
                appointmentDate,
                appointment.getAppointmentType()
        );

    }
    /**
     * Finds appointments by guidance staff ID and status.
     *
     * @param guidanceStaffId the ID of the guidance staff
     * @param status the appointment status to filter by
     * @return list of appointments matching the criteria
     */
    @Override
    public List<Appointment> findAppointmentByStatus(Long guidanceStaffId, String status) {
        return appointmentRepository.findByGuidanceStaff_IdAndStatusIgnoreCase(guidanceStaffId, status);
    }

    /**
     * Finds an appointment by its ID.
     *
     * @param appointmentId the appointment ID
     * @return the appointment
     * @throws AppointmentNotFoundException if appointment is not found
     */
    @Override
    public Appointment findAppointmentsByAppointmentId(Long appointmentId) {
        return findAppointmentById(appointmentId);
    }

    /**
     * Gets all appointments for a specific guidance staff member.
     *
     * @param guidanceStaffId the guidance staff ID
     * @return list of appointments
     */
    @Override
    public List<Appointment> getAppointmentByGuidanceStaff(Long guidanceStaffId) {
        return appointmentRepository.findByGuidanceStaff_Id(guidanceStaffId);
    }

    /**
     * Gets all appointments for a specific student.
     *
     * @param studentId the student ID
     * @return list of appointments
     */
    @Override
    public List<Appointment> getAppointmentByStudent(Long studentId) {
        return appointmentRepository.findByStudent_Id(studentId);
    }
    /**
     * Finds student appointments filtered by status.
     *
     * @param studentId the student ID
     * @param status the appointment status
     * @return list of appointments matching the criteria
     */
    @Override
    public List<Appointment> findStudentAppointments(Long studentId, String status) {
        return appointmentRepository.findByStudent_IdAndStatusIgnoreCase(studentId, status);
    }

    /**
     * Gets booked time slots for the authenticated guidance staff on a specific date.
     *
     * @param date the date in ISO format (yyyy-MM-dd)
     * @return list of booked slots with start and end times
     */
    @Override
    public List<BookedSlotsResponse> getBookedSlots(String date) {
        GuidanceStaff guidanceStaff = guidanceService.findAuthenticatedGuidanceStaff();
        LocalDate targetDate = LocalDate.parse(date);

        List<Appointment> appointments = appointmentRepository
                .findByGuidanceStaff_IdAndScheduledDateBetween(
                        guidanceStaff.getId(),
                        targetDate.atStartOfDay(),
                        targetDate.atTime(23, 59, 59)
                );

        return appointments.stream()
                .filter(apt -> AppointmentStatus.SCHEDULED.name().equals(apt.getStatus()))
                .map(apt -> new BookedSlotsResponse(apt.getScheduledDate(), apt.getEndDate()))
                .collect(Collectors.toList());
    }


    private void validateAppointmentAvailability(Long studentId, Long guidanceStaffId,
                                                 LocalDateTime scheduledDate, LocalDateTime endDate) {
        validateStudentDayAvailability(studentId, scheduledDate);
        validateGuidanceStaffAvailability(guidanceStaffId, scheduledDate, endDate);
    }

    private void validateStudentDayAvailability(Long studentId, LocalDateTime scheduledDate) {
        LocalDateTime startOfDay = scheduledDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        boolean hasAppointment = appointmentRepository
                .existsByStudentIdAndStatusInAndScheduledDateBetween(
                        studentId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDay,
                        endOfDay
                );

        if (hasAppointment) {
            throw new AppointmentAlreadyExistException(
                    "Student already has an appointment on this day"
            );
        }
    }

    private void validateGuidanceStaffAvailability(Long guidanceStaffId,
                                                   LocalDateTime scheduledDate,
                                                   LocalDateTime endDate) {
        LocalDateTime startOfDay = scheduledDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<Appointment> staffAppointments = appointmentRepository
                .findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
                        guidanceStaffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDay,
                        endOfDay
                );

        for (Appointment appointment : staffAppointments) {
            if (hasTimeConflict(appointment, scheduledDate, endDate)) {
                throw new AppointmentAlreadyExistException(
                        "Guidance staff has an appointment during this time"
                );
            }
        }
    }

    private void validatePendingAppointmentLimit(Long studentId) {
        long pendingCount = appointmentRepository.countByStudent_IdAndStatusIn(
                studentId,
                Arrays.asList(AppointmentStatus.PENDING.name(),
                        AppointmentStatus.SCHEDULED.name(),
                        AppointmentStatus.ONGOING.name())
        );

        if (pendingCount >= maxPendingAppointments) {
            throw new RateLimitExceededException(
                    String.format(
                            "You have reached the maximum limit of %d pending appointment(s). " +
                                    "Please wait for existing appointments to be processed.",
                            maxPendingAppointments
                    )
            );
        }

        LOGGER.debug("Student {} has {} pending appointment(s), limit is {}",
                studentId, pendingCount, maxPendingAppointments);
    }

    private Appointment createAndSaveAppointment(Student student, GuidanceStaff guidanceStaff,
                                                 Appointment request) {
        Appointment newAppointment = new Appointment();
        newAppointment.setStudent(student);
        newAppointment.setGuidanceStaff(guidanceStaff);
        newAppointment.setScheduledDate(request.getScheduledDate());
        newAppointment.setEndDate(request.getEndDate());
        newAppointment.setAppointmentType(request.getAppointmentType());
        newAppointment.setNotes(request.getNotes());
        newAppointment.setStatus(AppointmentStatus.PENDING.name());
        newAppointment.setDateCreated(LocalDateTime.now());

        return appointmentRepository.save(newAppointment);
    }

    private void notifyStudent(Appointment appointment) {
        String message = AppointmentMessageBuilder.forStudent(
                appointment.getGuidanceStaff(),
                appointment
        );

        notificationService.sendNotificationToUser(
                appointment.getStudent().getUser().getUserId(),
                "Appointment Request",
                message,
                "APPOINTMENT_REQUEST"
        );

        notificationService.saveNotification(
                appointment.getStudent().getUser(),
                appointment,
                message,
                "APPOINTMENT_REQUEST"
        );
    }

    private void notifyGuidanceStaff(Appointment appointment) {
        String message = AppointmentMessageBuilder.forGuidance(
                appointment.getStudent(),
                appointment
        );

        notificationService.sendNotificationToUser(
                appointment.getGuidanceStaff().getUser().getUserId(),
                "Appointment Request",
                message,
                "APPOINTMENT_REQUEST"
        );

        notificationService.saveNotification(
                appointment.getGuidanceStaff().getUser(),
                appointment,
                message,
                "APPOINTMENT_REQUEST"
        );
    }

    private void notifyUserOfResponse(Appointment appointment, String action,
                                      boolean notifyGuidanceStaff) {
        String responderName = notifyGuidanceStaff
                ? getStudentFullName(appointment.getStudent())
                : getGuidanceStaffFullName(appointment.getGuidanceStaff());

        String message = AppointmentMessageBuilder.forGuidanceResponse(
                responderName,
                action,
                appointment
        );

        String recipientUserId = notifyGuidanceStaff
                ? appointment.getGuidanceStaff().getUser().getUserId()
                : appointment.getStudent().getUser().getUserId();

        notificationService.sendNotificationToUser(
                recipientUserId,
                "Appointment Response",
                message,
                action.toUpperCase()
        );

        Notifications notification = new Notifications();
        notification.setUser(notifyGuidanceStaff
                ? appointment.getGuidanceStaff().getUser()
                : appointment.getStudent().getUser());
        notification.setAppointment(appointment);
        notification.setMessage(message);
        notification.setActionType(action.toUpperCase());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(0);

        notificationRepository.save(notification);
    }

    private void validateAppointmentDates(LocalDateTime scheduled, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (scheduled == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (!scheduled.isBefore(end)) {
            throw new IllegalArgumentException("Scheduled date must be before end date");
        }
        if (scheduled.isBefore(now) || end.isBefore(now)) {
            throw new IllegalArgumentException("Dates cannot be in the past");
        }
    }

    private void validateAppointmentIsPending(Appointment appointment) {
        if (!AppointmentStatus.PENDING.name().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Appointment is not in PENDING status");
        }
    }

    private String extractAndValidateAction(Map<String, String> data) {
        String action = data.get("action");

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action is required");
        }

        String upperAction = action.toUpperCase();
        if (!upperAction.equals("ACCEPT") && !upperAction.equals("DECLINE")) {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        return upperAction;
    }

    private void updateAppointmentStatus(Appointment appointment, String action) {
        if ("ACCEPT".equals(action)) {
            appointment.setStatus(AppointmentStatus.SCHEDULED.name());
        } else {
            appointment.setStatus(AppointmentStatus.CANCELLED.name());
        }
    }

    private boolean hasTimeConflict(Appointment existing, LocalDateTime newStart,
                                    LocalDateTime newEnd) {
        return newStart.isBefore(existing.getEndDate()) &&
                newEnd.isAfter(existing.getScheduledDate());
    }

    private String determineAppointmentStatus(
            LocalDateTime start,
            LocalDateTime end,
            LocalDateTime now,
            String oldStatus
    ) {

        if (start == null || end == null) return oldStatus;

        if (!now.isBefore(end)) {
            return AppointmentStatus.COMPLETED.name();
        }

        if (!now.isBefore(start)) {
            return AppointmentStatus.ONGOING.name();
        }
        return AppointmentStatus.SCHEDULED.name();
    }


    private Student findStudentByNumber(String studentNumber) {
        Student student = studentRepository.findStudentByStudentNumber(studentNumber);
        if (student == null) {
            throw new StudentNotFoundException("Student not found");
        }
        return student;
    }

    private GuidanceStaff findGuidanceStaffById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Guidance Staff ID is required");
        }
        return guidanceStaffRepository.findById(id)
                .orElseThrow(() -> new GuidanceStaffNotFoundException("Guidance Staff not found"));
    }

    private Appointment findAppointmentById(Long appointmentId) {
        if (appointmentId == null) {
            throw new AppointmentNotFoundException("Appointment ID cannot be null");
        }
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
    }

    private String getStudentFullName(Student student) {
        return student.getPerson().getFirstName() + " " +
                student.getPerson().getLastName();
    }

    private String getGuidanceStaffFullName(GuidanceStaff staff) {
        return staff.getPerson().getFirstName() + " " +
                staff.getPerson().getLastName();
    }

        private LocalDateTime toPH(LocalDateTime date) {
            if (date == null) return null;

            return date
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Asia/Manila"))
                    .toLocalDateTime();
        }

    }
