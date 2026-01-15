package org.rocs.asa.service.appointment.reschedule;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.request.RescheduleAppointmentRequest;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.exception.domain.AppointmentAlreadyExistException;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
import org.rocs.asa.exception.domain.RescheduleLimitExceed;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.rocs.asa.service.appointment.notification.AppointmentNotificationService;
import org.rocs.asa.service.appointment.validator.AppointmentValidator;
import org.rocs.asa.service.student.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Handles appointment reschedule operations
 */
@Service
public class AppointmentRescheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentRescheduleService.class);
    private static final int MAX_RESCHEDULES = 1;
    private static final int MIN_RESCHEDULE_WINDOW_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final StudentService studentService;
    private final AppointmentValidator validator;
    private final AppointmentNotificationService notificationService;

    public AppointmentRescheduleService(
            AppointmentRepository appointmentRepository,
            StudentService studentService,
            AppointmentValidator validator,
            AppointmentNotificationService notificationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.studentService = studentService;
        this.validator = validator;
        this.notificationService = notificationService;
    }

    /**
     * Student requests to reschedule an appointment
     */
    @Transactional
    public Appointment requestReschedule(RescheduleAppointmentRequest request) {
        LOGGER.info("Student requesting reschedule for appointment: {}", request.getAppointmentId());

        Appointment appointment = findAppointmentById(request.getAppointmentId());
        Student student = studentService.findByAuthenticatedStudent();

        validateRescheduleEligibility(appointment);

        LocalDateTime newScheduledUTC = AppointmentMessageBuilder.toUTC(request.getNewScheduledDate());
        LocalDateTime newEndUTC = AppointmentMessageBuilder.toUTC(request.getNewEndDate());

        validator.validateAppointmentDates(newScheduledUTC, newEndUTC);

        validateNoBlockedSlot(appointment.getGuidanceStaff().getId(), newScheduledUTC, newEndUTC);
        validateAvailabilityForReschedule(appointment, student, newScheduledUTC, newEndUTC);

        if (appointment.getScheduledDate().equals(newScheduledUTC) &&
                appointment.getEndDate().equals(newEndUTC)) {
            throw new IllegalStateException("New appointment time is the same as current time");
        }

        String oldScheduledPH = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());
        String newScheduledPH = AppointmentMessageBuilder.formatFullDateTimePH(newScheduledUTC);

        String rescheduleData = buildRescheduleData(newScheduledUTC, newEndUTC, request.getReason());
        appointment.setNotes(
                (appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") + rescheduleData
        );
        appointment.setStatus("RESCHEDULE_PENDING");

        Appointment saved = appointmentRepository.save(appointment);

        notificationService.notifyRescheduleRequest(saved, oldScheduledPH, newScheduledPH, request.getReason());

        LOGGER.info("Reschedule request created for appointment: {}", saved.getAppointmentId());
        return saved;
    }

    /**
     * Counselor responds to reschedule request
     */
    @Transactional
    public Appointment respondToReschedule(Long appointmentId, Map<String, String> data) {
        LOGGER.info("Counselor responding to reschedule request: {}", appointmentId);

        Appointment appointment = findAppointmentById(appointmentId);

        if (!"RESCHEDULE_PENDING".equals(appointment.getStatus())) {
            throw new IllegalStateException("This appointment is not pending reschedule approval");
        }

        String action = validator.extractAndValidateAction(data);

        RescheduleData rescheduleData = extractRescheduleData(appointment);

        String oldScheduledPH = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());
        String newScheduledPH = AppointmentMessageBuilder.formatFullDateTimePH(rescheduleData.newScheduledUTC);

        if ("ACCEPT".equals(action)) {
            return acceptReschedule(appointment, rescheduleData, oldScheduledPH, newScheduledPH);
        } else {
            return declineReschedule(appointment, rescheduleData, oldScheduledPH, newScheduledPH);
        }
    }

    private Appointment acceptReschedule(
            Appointment appointment,
            RescheduleData data,
            String oldTime,
            String newTime
    ) {
        LOGGER.info("🟢 ACCEPTING reschedule for appointment: {}", appointment.getAppointmentId());
        LOGGER.info("   Current rescheduleCount (from notes): {}", appointment.getRescheduleCount());

        validator.validateGuidanceStaffAvailability(
                appointment.getGuidanceStaff().getId(),
                data.newScheduledUTC,
                data.newEndUTC
        );

        appointment.setScheduledDate(data.newScheduledUTC);
        appointment.setEndDate(data.newEndUTC);
        appointment.setStatus(AppointmentStatus.SCHEDULED.name());

        // ✅ Add APPROVED marker to notes (this increments rescheduleCount automatically)
        String approvalMarker = buildApprovalMarker(oldTime, newTime);
        String cleanedNotes = removeRescheduleRequestFromNotes(appointment.getNotes());
        appointment.setNotes((cleanedNotes.isEmpty() ? "" : cleanedNotes + "\n") + approvalMarker);

        Appointment saved = appointmentRepository.save(appointment);

        LOGGER.info("   New rescheduleCount (from notes): {}", saved.getRescheduleCount());
        LOGGER.info("🟢 Reschedule APPROVED for appointment: {}", appointment.getAppointmentId());

        notificationService.notifyRescheduleApproved(saved, oldTime, newTime);

        return saved;
    }

    private Appointment declineReschedule(
            Appointment appointment,
            RescheduleData data,
            String oldTime,
            String newTime
    ) {
        LOGGER.info("   Current rescheduleCount (from notes): {}", appointment.getRescheduleCount());

        appointment.setStatus(AppointmentStatus.SCHEDULED.name());
        String declineMarker = buildDeclineMarker(oldTime, newTime);
        String cleanedNotes = removeRescheduleRequestFromNotes(appointment.getNotes());
        appointment.setNotes((cleanedNotes.isEmpty() ? "" : cleanedNotes + "\n") + declineMarker);

        Appointment saved = appointmentRepository.save(appointment);

        LOGGER.info("   New rescheduleCount (from notes): {}", saved.getRescheduleCount());
        LOGGER.info("🔴 Reschedule DECLINED for appointment: {}", appointment.getAppointmentId());

        notificationService.notifyRescheduleDeclined(saved, oldTime, newTime);

        return saved;
    }


    private void validateRescheduleEligibility(Appointment appointment) {
        int rescheduleCount = appointment.getRescheduleCount();
        if (rescheduleCount >= MAX_RESCHEDULES) {
            throw new RescheduleLimitExceed(
                    "This appointment has already been rescheduled once. No further reschedules are allowed."
            );
        }

        if (!AppointmentStatus.SCHEDULED.name().equals(appointment.getStatus()) &&
                !AppointmentStatus.PENDING.name().equals(appointment.getStatus())) {
            throw new IllegalStateException("Only scheduled or pending appointments can be rescheduled");
        }

        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        if (appointment.getScheduledDate().isBefore(nowUTC)) {
            throw new IllegalStateException("Cannot reschedule an appointment that has already passed");
        }

        long minutesUntil = Duration.between(nowUTC, appointment.getScheduledDate()).toMinutes();
        if (minutesUntil < MIN_RESCHEDULE_WINDOW_MINUTES) {
            throw new IllegalStateException(
                    "Cannot reschedule appointments within 30 minutes of the scheduled time"
            );
        }
    }

    private void validateNoBlockedSlot(Long staffId, LocalDateTime startUTC, LocalDateTime endUTC) {
        if (isTimeSlotBlocked(staffId, startUTC, endUTC)) {
            throw new AppointmentAlreadyExistException(
                    "This time slot is blocked and unavailable for appointments"
            );
        }
    }

    private void validateAvailabilityForReschedule(
            Appointment appointment,
            Student student,
            LocalDateTime newScheduledUTC,
            LocalDateTime newEndUTC
    ) {
        validator.validateGuidanceStaffAvailabilityExcluding(
                appointment.getGuidanceStaff().getId(),
                newScheduledUTC,
                newEndUTC,
                appointment.getAppointmentId()
        );

        validateStudentTimeAvailabilityExcluding(
                student.getId(),
                newScheduledUTC,
                newEndUTC,
                appointment.getAppointmentId()
        );

        validateStudentCounselorDailyLimitExcluding(
                student.getId(),
                appointment.getGuidanceStaff().getId(),
                newScheduledUTC,
                appointment.getAppointmentId()
        );
    }

    private void validateStudentTimeAvailabilityExcluding(
            Long studentId,
            LocalDateTime newScheduledUTC,
            LocalDateTime newEndUTC,
            Long excludeAppointmentId
    ) {
        LocalDateTime startOfDayUTC = newScheduledUTC.toLocalDate().atStartOfDay();
        LocalDateTime endOfDayUTC = startOfDayUTC.plusDays(1).minusSeconds(1);

        List<Appointment> studentAppointments = appointmentRepository
                .findByStudent_IdAndStatusInAndScheduledDateBetween(
                        studentId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDayUTC,
                        endOfDayUTC
                );

        for (Appointment existing : studentAppointments) {
            if (existing.getAppointmentId().equals(excludeAppointmentId)) {
                continue;
            }

            if (hasTimeConflict(existing, newScheduledUTC, newEndUTC)) {
                throw new AppointmentAlreadyExistException(
                        "You have another appointment too close to this time"
                );
            }
        }
    }

    private void validateStudentCounselorDailyLimitExcluding(
            Long studentId,
            Long guidanceStaffId,
            LocalDateTime newScheduledUTC,
            Long excludeAppointmentId
    ) {
        LocalDateTime startOfDayUTC = newScheduledUTC.toLocalDate().atStartOfDay();
        LocalDateTime endOfDayUTC = startOfDayUTC.plusDays(1).minusSeconds(1);

        List<Appointment> appointments = appointmentRepository
                .findByStudent_IdAndGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
                        studentId,
                        guidanceStaffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDayUTC,
                        endOfDayUTC
                );

        for (Appointment existing : appointments) {
            if (!existing.getAppointmentId().equals(excludeAppointmentId)) {
                throw new AppointmentAlreadyExistException(
                        "You already have an appointment with this counselor on this day"
                );
            }
        }
    }


    private boolean isTimeSlotBlocked(Long staffId, LocalDateTime startUTC, LocalDateTime endUTC) {
        return appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
                        staffId,
                        List.of(AppointmentStatus.SCHEDULED.name(), "BLOCKED"),
                        endUTC,
                        startUTC
                );
    }

    private boolean hasTimeConflict(Appointment existing, LocalDateTime newStart, LocalDateTime newEnd) {
        return newStart.isBefore(existing.getEndDate()) && newEnd.isAfter(existing.getScheduledDate());
    }

    private String buildRescheduleData(LocalDateTime scheduledUTC, LocalDateTime endUTC, String reason) {
        LocalDateTime requestTimeUTC = AppointmentMessageBuilder.nowUTC();
        return String.format(
                "RESCHEDULE REQUEST|%s|%s|%s|%s",
                requestTimeUTC.toString(),
                scheduledUTC.toString(),
                endUTC.toString(),
                reason != null ? reason : ""
        );
    }

    private String buildApprovalMarker(String oldTime, String newTime) {
        LocalDateTime approvalTimeUTC = AppointmentMessageBuilder.nowUTC();
        return String.format(
                "RESCHEDULE_APPROVED|%s|%s|%s",
                approvalTimeUTC.toString(),
                oldTime,
                newTime
        );
    }

    private String buildDeclineMarker(String oldTime, String newTime) {
        LocalDateTime declineTimeUTC = AppointmentMessageBuilder.nowUTC();
        return String.format(
                "RESCHEDULE_DECLINED|%s|%s|%s",
                declineTimeUTC.toString(),
                oldTime,
                newTime
        );
    }

    private RescheduleData extractRescheduleData(Appointment appointment) {
        String[] notesLines = appointment.getNotes().split("\n");

        for (String line : notesLines) {
            if (line.startsWith("RESCHEDULE REQUEST|")) {
                String[] parts = line.split("\\|");
                if (parts.length < 4) {
                    throw new IllegalStateException("Invalid reschedule data format");
                }

                return new RescheduleData(
                        LocalDateTime.parse(parts[1]),
                        LocalDateTime.parse(parts[2]),
                        LocalDateTime.parse(parts[3]),
                        parts.length > 4 ? parts[4] : ""
                );
            }
        }

        throw new IllegalStateException("Reschedule request data not found");
    }

    private String removeRescheduleRequestFromNotes(String notes) {
        if (notes == null) return "";

        String[] lines = notes.split("\n");
        StringBuilder cleaned = new StringBuilder();

        for (String line : lines) {
            if (!line.startsWith("RESCHEDULE REQUEST|")) {
                if (cleaned.length() > 0) cleaned.append("\n");
                cleaned.append(line);
            }
        }

        return cleaned.toString().trim();
    }

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));
    }


    private static class RescheduleData {
        final LocalDateTime requestTime;
        final LocalDateTime newScheduledUTC;
        final LocalDateTime newEndUTC;
        final String reason;

        RescheduleData(LocalDateTime requestTime, LocalDateTime newScheduledUTC,
                       LocalDateTime newEndUTC, String reason) {
            this.requestTime = requestTime;
            this.newScheduledUTC = newScheduledUTC;
            this.newEndUTC = newEndUTC;
            this.reason = reason;
        }
    }
}