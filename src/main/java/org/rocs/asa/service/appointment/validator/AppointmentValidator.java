package org.rocs.asa.service.appointment.validator;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.exception.domain.AppointmentAlreadyExistException;
import org.rocs.asa.exception.domain.RateLimitExceededException;
import org.rocs.asa.exception.domain.WeekEndException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.service.appointment.buffer.AppointmentBuffer;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralized validation for all appointment-related business rules
 */
@Component
public class AppointmentValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentValidator.class);
    private static final int MAX_PENDING_APPOINTMENTS = 3;
    private static final int MIN_DURATION_MINUTES = 30;
    private static final int WORK_START_HOUR = 8;
    private static final int WORK_END_HOUR = 17;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentBuffer appointmentBuffer;

    public AppointmentValidator(AppointmentRepository appointmentRepository,
                                AppointmentBuffer appointmentBuffer) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentBuffer = appointmentBuffer;
    }

    /**
     * Validates appointment dates and times
     */
    public void validateAppointmentDates(LocalDateTime scheduledUTC, LocalDateTime endUTC) {
        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();

        if (scheduledUTC == null || endUTC == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (!scheduledUTC.isBefore(endUTC)) {
            throw new IllegalArgumentException("Scheduled date must be before end date");
        }

        if (scheduledUTC.isBefore(nowUTC)) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        if (endUTC.isBefore(nowUTC)) {
            throw new IllegalArgumentException("End time cannot be in the past");
        }

        LocalDateTime scheduledPH = AppointmentMessageBuilder.toPH(scheduledUTC);
        LocalDateTime endPH = AppointmentMessageBuilder.toPH(endUTC);

        DayOfWeek dayOfWeek = scheduledPH.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new WeekEndException("Appointments cannot be scheduled on weekends");
        }

        if (!scheduledPH.toLocalDate().equals(endPH.toLocalDate())) {
            throw new IllegalArgumentException("Appointment must start and end on the same day");
        }

        validateBusinessHours(scheduledPH, endPH);

        validateMinimumDuration(scheduledPH, endPH);
    }

    /**
     * Validates business hours (8 AM - 5 PM PH time)
     */
    private void validateBusinessHours(LocalDateTime scheduledPH, LocalDateTime endPH) {
        int startHour = scheduledPH.getHour();
        int endHour = endPH.getHour();
        int endMinute = endPH.getMinute();

        if (startHour < WORK_START_HOUR) {
            throw new IllegalArgumentException("Start time cannot be before 8:00 AM (PH time)");
        }

        if (startHour >= WORK_END_HOUR) {
            throw new IllegalArgumentException("Start time must be before 5:00 PM (PH time)");
        }

        if (endHour < WORK_START_HOUR) {
            throw new IllegalArgumentException("End time must be after 8:00 AM (PH time)");
        }

        if (endHour > WORK_END_HOUR || (endHour == WORK_END_HOUR && endMinute > 0)) {
            throw new IllegalArgumentException("End time must be no later than 5:00 PM (PH time)");
        }
    }

    /**
     * Validates minimum appointment duration (30 minutes)
     */
    private void validateMinimumDuration(LocalDateTime start, LocalDateTime end) {
        long durationMinutes = Duration.between(start, end).toMinutes();
        if (durationMinutes < MIN_DURATION_MINUTES) {
            throw new IllegalArgumentException("Appointment must be at least 30 minutes long");
        }
    }

    /**
     * Validates pending appointment limit for a student
     */
    public void validatePendingAppointmentLimit(Long studentId) {
        long pendingCount = appointmentRepository.countByStudent_IdAndStatus(
                studentId,
                AppointmentStatus.PENDING.name()
        );

        if (pendingCount >= MAX_PENDING_APPOINTMENTS) {
            throw new RateLimitExceededException(
                    String.format(
                            "You have reached the maximum limit of %d pending appointment(s). " +
                                    "Please wait for existing appointments to be processed.",
                            MAX_PENDING_APPOINTMENTS
                    )
            );
        }

        LOGGER.debug("Student {} has {} pending appointment(s), limit is {}",
                studentId, pendingCount, MAX_PENDING_APPOINTMENTS);
    }

    /**
     * Validates that appointment is in PENDING status
     */
    public void validateAppointmentIsPending(Appointment appointment) {
        if (!AppointmentStatus.PENDING.name().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Appointment is not in PENDING status");
        }
    }

    /**
     * Validates that student doesn't have another appointment with same counselor on same day
     */
    public void validateStudentCounselorDailyLimit(
            Long studentId,
            Long guidanceStaffId,
            LocalDateTime startUTC
    ) {
        LocalDateTime dayStart = startUTC.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

        boolean exists = appointmentRepository
                .existsByStudentIdAndGuidanceStaffIdAndStatusInAndScheduledDateBetween(
                        studentId,
                        guidanceStaffId,
                        List.of(
                                AppointmentStatus.PENDING.name(),
                                AppointmentStatus.SCHEDULED.name()
                        ),
                        dayStart,
                        dayEnd
                );

        if (exists) {
            throw new AppointmentAlreadyExistException(
                    "You already have an appointment with this counselor for this day"
            );
        }
    }

    /**
     * Validates that time slot is not blocked
     */
    public void validateTimeSlotNotBlocked(Long staffId, LocalDateTime startUTC, LocalDateTime endUTC) {
        boolean isBlocked = appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
                        staffId,
                        List.of(AppointmentStatus.SCHEDULED.name(), "BLOCKED"),
                        endUTC,
                        startUTC
                );

        if (isBlocked) {
            throw new AppointmentAlreadyExistException(
                    "This time slot is blocked and unavailable for appointments"
            );
        }
    }

    /**
     * Validates student time availability with buffer
     */
    public void validateStudentTimeAvailability(
            Long studentId,
            LocalDateTime startUTC,
            LocalDateTime endUTC
    ) {
        LocalDateTime bufferedStart = appointmentBuffer.applyBufferBefore(startUTC);
        LocalDateTime bufferedEnd = appointmentBuffer.applyBufferAfter(endUTC);

        boolean conflict = appointmentRepository
                .existsByStudentIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
                        studentId,
                        List.of(
                                AppointmentStatus.PENDING.name(),
                                AppointmentStatus.SCHEDULED.name()
                        ),
                        bufferedEnd,
                        bufferedStart
                );

        if (conflict) {
            throw new AppointmentAlreadyExistException(
                    "You have another appointment too close to this time (buffer required)"
            );
        }
    }

    /**
     * Validates guidance staff availability (no overlapping appointments)
     */
    public void validateGuidanceStaffAvailability(
            Long guidanceStaffId,
            LocalDateTime scheduledDateUTC,
            LocalDateTime endDateUTC
    ) {
        validateTimeSlotNotBlocked(guidanceStaffId, scheduledDateUTC, endDateUTC);

        LocalDateTime startOfDayUTC = scheduledDateUTC.toLocalDate().atStartOfDay();
        LocalDateTime endOfDayUTC = startOfDayUTC.plusDays(1).minusSeconds(1);

        List<Appointment> staffAppointments = appointmentRepository
                .findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
                        guidanceStaffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDayUTC,
                        endOfDayUTC
                );

        for (Appointment appointment : staffAppointments) {
            if (hasTimeConflict(appointment, scheduledDateUTC, endDateUTC)) {
                throw new AppointmentAlreadyExistException(
                        "Guidance staff has an appointment during this time"
                );
            }
        }
    }

    /**
     * Validates guidance staff availability excluding a specific appointment (for updates)
     */
    public void validateGuidanceStaffAvailabilityExcluding(
            Long guidanceStaffId,
            LocalDateTime scheduledDateUTC,
            LocalDateTime endDateUTC,
            Long excludeAppointmentId
    ) {
        validateTimeSlotNotBlocked(guidanceStaffId, scheduledDateUTC, endDateUTC);

        LocalDateTime startOfDayUTC = scheduledDateUTC.toLocalDate().atStartOfDay();
        LocalDateTime endOfDayUTC = startOfDayUTC.plusDays(1).minusSeconds(1);

        List<Appointment> staffAppointments = appointmentRepository
                .findByGuidanceStaff_IdAndStatusInAndScheduledDateBetween(
                        guidanceStaffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        startOfDayUTC,
                        endOfDayUTC
                );

        for (Appointment existing : staffAppointments) {
            if (existing.getAppointmentId().equals(excludeAppointmentId)) {
                continue;
            }

            if (hasTimeConflict(existing, scheduledDateUTC, endDateUTC)) {
                throw new AppointmentAlreadyExistException(
                        "Counselor has another appointment during this time"
                );
            }
        }
    }

    /**
     * Checks if there's a time conflict between appointments
     */
    private boolean hasTimeConflict(Appointment existing, LocalDateTime newStartUTC, LocalDateTime newEndUTC) {
        return newStartUTC.isBefore(existing.getEndDate()) &&
                newEndUTC.isAfter(existing.getScheduledDate());
    }

    /**
     * Validates action parameter (ACCEPT or DECLINE)
     */
    public String extractAndValidateAction(java.util.Map<String, String> data) {
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
}