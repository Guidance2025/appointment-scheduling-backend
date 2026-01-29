package org.rocs.asa.service.appointment.creation;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.exception.domain.StudentNotFoundException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.rocs.asa.service.appointment.notification.AppointmentNotificationService;
import org.rocs.asa.service.appointment.validator.AppointmentValidator;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.student.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles appointment creation operations
 */
@Service
public class AppointmentCreationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentCreationService.class);

    private final AppointmentRepository appointmentRepository;
    private final StudentRepository studentRepository;
    private final GuidanceStaffRepository guidanceStaffRepository;
    private final AppointmentValidator validator;
    private final AppointmentNotificationService notificationService;
    private final GuidanceService guidanceService;
    private final StudentService studentService;

    public AppointmentCreationService(
            AppointmentRepository appointmentRepository,
            StudentRepository studentRepository,
            GuidanceStaffRepository guidanceStaffRepository,
            AppointmentValidator validator,
            AppointmentNotificationService notificationService,
            GuidanceService guidanceService,
            StudentService studentService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.validator = validator;
        this.notificationService = notificationService;
        this.guidanceService = guidanceService;
        this.studentService = studentService;
    }

    /**
     * Creates an appointment initiated by guidance staff
     */
    @Transactional
    public Appointment createByStaff(Appointment request) {
        LOGGER.info("Guidance staff creating appointment");

        LocalDateTime scheduledDateUTC = AppointmentMessageBuilder.toUTC(request.getScheduledDate());
        LocalDateTime endDateUTC = AppointmentMessageBuilder.toUTC(request.getEndDate());

        validator.validateAppointmentDates(scheduledDateUTC, endDateUTC);

        Student student = findStudentByNumber(request.getStudent().getStudentNumber());
        GuidanceStaff guidanceStaff = guidanceService.findAuthenticatedGuidanceStaff();

        validator.validatePendingAppointmentLimit(student.getId());
        validator.validateStudentCounselorDailyLimit(student.getId(), guidanceStaff.getId(), scheduledDateUTC);
        validator.validateStudentTimeAvailability(student.getId(), scheduledDateUTC, endDateUTC);
        validator.validateGuidanceStaffAvailability(guidanceStaff.getId(), scheduledDateUTC, endDateUTC);

        Appointment saved = createAndSave(student, guidanceStaff, request, scheduledDateUTC, endDateUTC);

        notificationService.notifyStudentOfRequest(saved);

        LOGGER.info("Appointment created by staff. ID: {}", saved.getAppointmentId());
        return saved;
    }

    /**
     * Creates an appointment initiated by student
     */
    @Transactional
    public Appointment createByStudent(Appointment request) {
        LOGGER.info("Student creating appointment");

        LocalDateTime scheduledUTC = AppointmentMessageBuilder.toUTC(request.getScheduledDate());
        LocalDateTime endUTC = AppointmentMessageBuilder.toUTC(request.getEndDate());

        validator.validateAppointmentDates(scheduledUTC, endUTC);

        Student student = studentService.findByAuthenticatedStudent();
        if (student == null) {
            throw new StudentNotFoundException("Student not found");
        }

        GuidanceStaff guidanceStaff = findGuidanceStaffById(request.getGuidanceStaff().getId());

        validator.validateStudentCounselorDailyLimit(student.getId(), guidanceStaff.getId(), scheduledUTC);
        validator.validateStudentTimeAvailability(student.getId(), scheduledUTC, endUTC);
        validator.validateGuidanceStaffAvailability(guidanceStaff.getId(), scheduledUTC, endUTC);

        Appointment saved = createAndSave(student, guidanceStaff, request, scheduledUTC, endUTC);

        notificationService.notifyGuidanceStaffOfRequest(saved);

        LOGGER.info("Appointment created by student. ID: {}", saved.getAppointmentId());
        return saved;
    }
    /**
     * Creates and saves an appointment entity
     */
    private Appointment createAndSave(
            Student student,
            GuidanceStaff guidanceStaff,
            Appointment request,
            LocalDateTime scheduledDateUTC,
            LocalDateTime endDateUTC
    ) {
        Appointment appointment = new Appointment();
        appointment.setStudent(student);
        appointment.setGuidanceStaff(guidanceStaff);
        appointment.setScheduledDate(scheduledDateUTC);
        appointment.setEndDate(endDateUTC);
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING.name());
        appointment.setDateCreated(AppointmentMessageBuilder.nowUTC());
        appointment.setRescheduleCount(0);

        return appointmentRepository.save(appointment);
    }

    private Student findStudentByNumber(String studentNumber) {
        Student student = studentRepository.findStudentByStudentNumber(studentNumber);
        if (student == null) {
            throw new StudentNotFoundException("Student not found with number: " + studentNumber);
        }
        return student;
    }

    private GuidanceStaff findGuidanceStaffById(Long id) {
        return guidanceStaffRepository.findById(id)
                .orElseThrow(() -> new GuidanceStaffNotFoundException("Guidance staff not found with ID: " + id));
    }
}