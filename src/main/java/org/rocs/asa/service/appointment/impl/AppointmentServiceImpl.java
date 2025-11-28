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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static Logger LOGGER = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final GuidanceService guidanceService;
    private final StudentService studentService;
    private final GuidanceStaffRepository guidanceStaffRepository;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  StudentRepository studentRepository,
                                  NotificationService notificationService,
                                  NotificationRepository notificationRepository
            , GuidanceService guidanceService, StudentService studentService,
                                  GuidanceStaffRepository guidanceStaffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.guidanceService = guidanceService;
        this.studentService = studentService;
        this.guidanceStaffRepository = guidanceStaffRepository;
    }

    @Override
    @Transactional
    public Appointment createAppointment(Appointment request) {

        validateAppointmentDates(request.getScheduledDate(), request.getEndDate());

        Student student = studentRepository.findStudentByStudentNumber(
                request.getStudent().getStudentNumber());
        if (student == null) throw new StudentNotFoundException("Student not found");

        LocalDateTime startOfDay = request.getScheduledDate().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        boolean hasAppointment = appointmentRepository
                .existsByStudentIdAndStatusInAndScheduledDateBetween(
                        student.getId(),
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDay,
                        endOfDay
                );
        if (hasAppointment)
            throw new AppointmentAlreadyExistException("Student already has an appointment on this day");

        GuidanceStaff guidanceStaff = guidanceService.findAuthenticatedGuidanceStaff();

        List<Appointment> staffAppointments = appointmentRepository
                .findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(guidanceStaff.getId(),
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDay,
                        endOfDay
                );

        for (Appointment appointment : staffAppointments) {
            if (isTimeConflict(appointment.getScheduledDate(), appointment.getEndDate(), request.getScheduledDate(), request.getEndDate())) {
                LOGGER.info("Student has an appointment for this time");
                throw new AppointmentAlreadyExistException("Student has an appointment for this time");
            }
        }

        Appointment newAppointment = new Appointment();
        newAppointment.setStudent(student);
        newAppointment.setGuidanceStaff(guidanceStaff);
        newAppointment.setScheduledDate(request.getScheduledDate());
        newAppointment.setEndDate(request.getEndDate());
        newAppointment.setAppointmentType(request.getAppointmentType());
        newAppointment.setNotes(request.getNotes());
        newAppointment.setStatus(AppointmentStatus.PENDING.name());
        newAppointment.setDateCreated(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(newAppointment);


        String notificationMsg = AppointmentMessageBuilder.forStudent(guidanceStaff, saved);
        notificationService.sendNotificationToUser(
                student.getUser().getUserId(),
                "Appointment Request",
                notificationMsg,
                "APPOINTMENT REQUEST"
        );
        notificationService.saveNotification(student.getUser(), saved, notificationMsg, "APPOINTMENT_REQUEST");

        return saved;
    }

    @Override
    public Appointment studentCreateAppointment(Appointment appointment) {

        validateAppointmentDates(appointment.getScheduledDate(), appointment.getEndDate());

        Student student = this.studentService.findByAuthenticatedStudent();

        if (student == null) {
            throw new AppointmentNotFoundException("Student can't set Appointment");
        }

        if (appointment.getGuidanceStaff() == null || appointment.getGuidanceStaff().getId() == null) {
            throw new IllegalArgumentException("Guidance Staff ID is required");
        }

        LocalDateTime startOfDay = appointment.getScheduledDate().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(
                appointment.getGuidanceStaff().getId()
        ).orElseThrow(() -> new GuidanceStaffNotFoundException("Guidance Staff does not exist"));

          List<Appointment> staffAppointment = appointmentRepository.findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
                  appointment.getGuidanceStaff().getId(),
                  List.of(AppointmentStatus.PENDING.name(),AppointmentStatus.SCHEDULED.name(),AppointmentStatus.ONGOING.name()),
                  startOfDay,
                  endOfDay
          );

          for(Appointment appointmentList : staffAppointment) {
              if (isTimeConflict(appointment.getScheduledDate(), appointment.getEndDate(), appointmentList.getScheduledDate(), appointmentList.getEndDate())) {
                  LOGGER.info("Guidance staff has an appointment for this time");
                  throw new AppointmentAlreadyExistException("Guidance Staff has an appointment for this time");
              }
          }

        Appointment newAppointment = new Appointment();
        newAppointment.setStudent(student);
        newAppointment.setGuidanceStaff(guidanceStaff);
        newAppointment.setScheduledDate(appointment.getScheduledDate());
        newAppointment.setEndDate(appointment.getEndDate());
        newAppointment.setAppointmentType(appointment.getAppointmentType());
        newAppointment.setNotes(appointment.getNotes());
        newAppointment.setStatus(AppointmentStatus.PENDING.name());
        newAppointment.setDateCreated(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(newAppointment);

        String notificationMsg = AppointmentMessageBuilder.forGuidance(student, saved);

        notificationService.sendNotificationToUser(
                guidanceStaff.getUser().getUserId(),
                "Appointment Request",
                notificationMsg,
                "APPOINTMENT_REQUEST"
        );

        notificationService.saveNotification(guidanceStaff.getUser(), saved, notificationMsg, "APPOINTMENT_REQUEST");

        return saved;
    }

    @Override
    public Appointment guidanceStaffResponse(Long appointmentId, Map<String, String> data) {

        Appointment appointment = findAppointmentsByAppointmentId(appointmentId);

        GuidanceStaff guidanceStaff = appointment.getGuidanceStaff();
        if (!guidanceStaff.getUser().getUserId().equals(guidanceStaff.getUser().getUserId()))
            throw new IllegalArgumentException("Unauthorized");
        if (!AppointmentStatus.PENDING.name().equalsIgnoreCase(appointment.getStatus()))
            throw new IllegalArgumentException("Appointment is not in PENDING status");

        String action = data.get("action");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("Action is required");

        switch (action.toUpperCase()) {
            case "ACCEPT" -> appointment.setStatus(AppointmentStatus.SCHEDULED.name());
            case "DECLINE" -> appointment.setStatus(AppointmentStatus.CANCELLED.name());
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        }
        String guidanceStaffName = appointment.getGuidanceStaff().getPerson().getFirstName() + " " +
                appointment.getGuidanceStaff().getPerson().getLastName();

        String notificationMsg = AppointmentMessageBuilder.forGuidanceResponse(
                guidanceStaffName,
                action,
                appointment
        );


        notificationService.sendNotificationToUser(
                appointment.getStudent().getUser().getUserId(),
                "Appointment Response",
                notificationMsg,
                action.toUpperCase()
        );

        appointmentRepository.save(appointment);

        Notifications notification = new Notifications();
        notification.setUser(appointment.getStudent().getUser());
        notification.setAppointment(appointment);
        notification.setMessage(notificationMsg);
        notification.setActionType(action.toUpperCase());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(0);
        notificationRepository.save(notification);

        return appointment;
    }

    @Override
    @Transactional
    public Appointment studentResponseToAppointment(Long appointmentId, Map<String, String> data) {

        Appointment appointment = findAppointmentsByAppointmentId(appointmentId);


        Student student = appointment.getStudent();
        if (!student.getUser().getUserId().equals(student.getUser().getUserId()))
            throw new IllegalArgumentException("Unauthorized");
        if (!AppointmentStatus.PENDING.name().equalsIgnoreCase(appointment.getStatus()))
            throw new IllegalArgumentException("Appointment is not in PENDING status");

        String action = data.get("action");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("Action is required");

        switch (action.toUpperCase()) {
            case "ACCEPT" -> appointment.setStatus(AppointmentStatus.SCHEDULED.name());
            case "DECLINE" -> appointment.setStatus(AppointmentStatus.CANCELLED.name());
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        }
        String studentName = appointment.getStudent().getPerson().getFirstName() + " " +
                appointment.getStudent().getPerson().getLastName();

        String notificationMsg = AppointmentMessageBuilder.forGuidanceResponse(
                studentName,
                action,
                appointment
        );


        notificationService.sendNotificationToUser(
                appointment.getGuidanceStaff().getUser().getUserId(),
                "Appointment Response",
                notificationMsg,
                action.toUpperCase()
        );

        appointmentRepository.save(appointment);

        Notifications notification = new Notifications();
        notification.setUser(appointment.getGuidanceStaff().getUser());
        notification.setAppointment(appointment);
        notification.setMessage(notificationMsg);
        notification.setActionType(action.toUpperCase());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(0);
        notificationRepository.save(notification);

        return appointment;
    }


    @Override
    public List<Appointment> findAppointmentByStatus(Long guidanceStaffId, String status) {
        return appointmentRepository.findByGuidanceStaff_IdAndStatusIgnoreCase(guidanceStaffId, status);
    }

    @Override
    public Appointment findAppointmentsByAppointmentId(Long appointmentId) {
        if (appointmentId == null) throw new AppointmentNotFoundException("Appointment ID cannot be null");
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
    }

    @Override
    public List<Appointment> getAppointmentByGuidanceStaff(Long employeeNumber) {
        return appointmentRepository.findByGuidanceStaff_Id(employeeNumber);
    }

    @Override
    public List<Appointment> getAppointmentByStudent(Long id) {
        return appointmentRepository.findByStudent_Id(id);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional
    public void markAsOnGoingOrIsCompleted() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = appointmentRepository.findByStatusInOptimized(List.of(AppointmentStatus.SCHEDULED.name(), AppointmentStatus.ONGOING.name()));
        List<Appointment> toUpdate = new ArrayList<>();

        for (Appointment appointment : appointments) {
            String oldStatus = appointment.getStatus();
            if (now.isAfter(appointment.getEndDate())) appointment.setStatus(AppointmentStatus.COMPLETED.name());
            else if (now.isAfter(appointment.getScheduledDate()) && now.isBefore(appointment.getEndDate()))
                appointment.setStatus(AppointmentStatus.ONGOING.name());

            if (!oldStatus.equals(appointment.getStatus())) toUpdate.add(appointment);
        }

        if (!toUpdate.isEmpty()) appointmentRepository.saveAll(toUpdate);
    }

    @Override
    public List<BookedSlotsResponse> getBookedSlots(String date) {
        GuidanceStaff guidanceStaff = this.guidanceService.findAuthenticatedGuidanceStaff();
        LocalDate targetDate = LocalDate.parse(date);

        List<Appointment> appointments = this.appointmentRepository
                .findByGuidanceStaff_IdAndScheduledDateBetween(
                        guidanceStaff.getId(),
                        targetDate.atStartOfDay(),
                        targetDate.atTime(23, 59, 59)
                );

        return appointments.stream()
                .filter(apt -> apt.getStatus().equals(AppointmentStatus.SCHEDULED.name()))
                .map(apt -> new BookedSlotsResponse(apt.getScheduledDate(), apt.getEndDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findStudentAppointments(Long studentId, String status) {
        return appointmentRepository.findByStudent_IdAndStatusIgnoreCase(studentId, status);
    }


    private void validateAppointmentDates(LocalDateTime scheduled, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (scheduled == null || end == null) throw new IllegalArgumentException("Dates cannot be null");
        if (!scheduled.isBefore(end)) throw new IllegalArgumentException("Scheduled date must be before end date");
        if (scheduled.isBefore(now) || end.isBefore(now))
            throw new IllegalArgumentException("Dates cannot be in the past");
    }

    private boolean isTimeConflict(LocalDateTime existingStart, LocalDateTime existingEnd, LocalDateTime newStart, LocalDateTime newEnd) {
        return (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart));
    }
}
