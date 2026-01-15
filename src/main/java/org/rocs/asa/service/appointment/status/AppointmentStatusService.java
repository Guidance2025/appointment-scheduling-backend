package org.rocs.asa.service.appointment.status;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.notification.NotificationRepository;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.rocs.asa.service.appointment.notification.AppointmentNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles appointment status updates and scheduled maintenance tasks
 */
@Service
public class AppointmentStatusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentStatusService.class);
    private static final int REMINDER_MINUTES_BEFORE = 25;
    private static final int REMINDER_WINDOW_MINUTES = 5;
    private static final int PENDING_EXPIRY_MINUTES = 2;
    private static final int RESCHEDULE_EXPIRY_MINUTES = 5;

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final AppointmentNotificationService notificationService;

    public AppointmentStatusService(
            AppointmentRepository appointmentRepository,
            NotificationRepository notificationRepository,
            AppointmentNotificationService notificationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Updates appointment statuses to ONGOING or COMPLETED based on current time
     */
    @Transactional
    public void updateOngoingAndCompleted() {
        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();

        List<Appointment> appointments = appointmentRepository.findByStatusInOptimized(
                List.of(AppointmentStatus.SCHEDULED.name(), AppointmentStatus.ONGOING.name())
        );

        for (Appointment appt : appointments) {
            if (AppointmentStatus.CANCELLED.name().equals(appt.getStatus()) ||
                    "EXPIRED".equals(appt.getStatus())) {
                continue;
            }

            String newStatus = determineStatus(
                    appt.getScheduledDate(),
                    appt.getEndDate(),
                    nowUTC,
                    appt.getStatus()
            );

            if (!appt.getStatus().equals(newStatus)) {
                appt.setStatus(newStatus);
            }
        }

        appointmentRepository.saveAll(appointments);
    }

    /**
     * Expires pending appointment requests after timeout
     */
    @Transactional
    public void expirePendingRequests() {
        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        LocalDateTime threshold = nowUTC.minusMinutes(PENDING_EXPIRY_MINUTES);

        List<Appointment> expired = appointmentRepository
                .findByStatusAndDateCreatedBefore(AppointmentStatus.PENDING.name(), threshold);

        if (!expired.isEmpty()) {
            for (Appointment appointment : expired) {
                appointment.setStatus("EXPIRED");
                notificationService.notifyAppointmentExpiration(appointment);
                markOldNotificationsAsRead(appointment, "APPOINTMENT_REQUEST");
            }

            appointmentRepository.saveAll(expired);
            LOGGER.info("Expired {} pending requests older than {} minutes",
                    expired.size(), PENDING_EXPIRY_MINUTES);
        }
    }

    /**
     * Expires reschedule pending requests after timeout
     */
    @Transactional
    public void expireReschedulePendingRequests() {
        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        LocalDateTime threshold = nowUTC.minusMinutes(RESCHEDULE_EXPIRY_MINUTES);

        List<Appointment> reschedulePending = appointmentRepository.findByStatus("RESCHEDULE_PENDING");
        List<Appointment> expired = new ArrayList<>();

        for (Appointment appointment : reschedulePending) {
            LocalDateTime requestTime = extractRescheduleRequestTime(appointment);

            if (requestTime != null && requestTime.isBefore(threshold)) {
                appointment.setStatus(AppointmentStatus.SCHEDULED.name());
                appointment.setNotes(removeRescheduleRequestFromNotes(appointment.getNotes()));
                notificationService.notifyRescheduleExpiration(appointment);
                expired.add(appointment);
            }
        }

        if (!expired.isEmpty()) {
            appointmentRepository.saveAll(expired);
            LOGGER.info("Expired {} reschedule requests older than {} minutes",
                    expired.size(), RESCHEDULE_EXPIRY_MINUTES);
        }
    }

    /**
     * Sends appointment reminders to students and counselors
     */
    @Transactional
    public void sendAppointmentReminders() {
        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        LocalDateTime reminderStart = nowUTC.minusMinutes(REMINDER_WINDOW_MINUTES);
        LocalDateTime reminderEnd = nowUTC.plusMinutes(REMINDER_MINUTES_BEFORE + REMINDER_WINDOW_MINUTES);

        List<Appointment> upcoming = appointmentRepository
                .findByStatusAndScheduledDateBetween(
                        AppointmentStatus.SCHEDULED.name(),
                        reminderStart,
                        reminderEnd
                );

        for (Appointment appointment : upcoming) {
            sendReminderIfNeeded(appointment, nowUTC);
        }
    }
    /**
     * Cleans up expired availability blocks
     */
    @Transactional
    public void cleanupExpiredAvailabilityBlocks() {
        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        LocalDateTime nowPH = AppointmentMessageBuilder.toPH(nowUTC);
        LocalDateTime startOfTodayUTC = AppointmentMessageBuilder.toUTC(nowPH.toLocalDate().atStartOfDay());

        LOGGER.info("Cleanup running - Now UTC: {}, Start of today UTC: {}", nowUTC, startOfTodayUTC);

        List<Appointment> expiredBlocks = appointmentRepository.findExpiredAvailabilityBlocks(
                "AVAILABILITY_BLOCK",
                "BLOCKED",
                nowUTC,
                startOfTodayUTC
        );

        if (!expiredBlocks.isEmpty()) {
            LOGGER.info("Found {} expired blocks to mark as EXPIRED", expiredBlocks.size());
            expiredBlocks.forEach(block -> {
                LOGGER.info("Expiring block: ID={}, scheduledDate={}, endDate={}",
                        block.getAppointmentId(), block.getScheduledDate(), block.getEndDate());
                block.setStatus("EXPIRED");
            });

            appointmentRepository.saveAll(expiredBlocks);
            LOGGER.info("Successfully marked {} availability blocks as EXPIRED", expiredBlocks.size());
        }
    }

    private String determineStatus(
            LocalDateTime startUTC,
            LocalDateTime endUTC,
            LocalDateTime nowUTC,
            String oldStatus
    ) {
        if (startUTC == null || endUTC == null) return oldStatus;

        if (!nowUTC.isBefore(endUTC)) {
            return AppointmentStatus.COMPLETED.name();
        }

        if (!nowUTC.isBefore(startUTC)) {
            return AppointmentStatus.ONGOING.name();
        }

        return AppointmentStatus.SCHEDULED.name();
    }




    private void sendReminderIfNeeded(Appointment appointment, LocalDateTime nowUTC) {
        long durationMinutes = Duration.between(
                appointment.getScheduledDate(),
                appointment.getEndDate()
        ).toMinutes();

        int reminderMinutes = durationMinutes >= 30 ? 25 : 10;
        LocalDateTime reminderTime = appointment.getScheduledDate().minusMinutes(reminderMinutes);

        if (nowUTC.isAfter(reminderTime) && nowUTC.isBefore(appointment.getScheduledDate())) {
            if (shouldSendReminder(appointment)) {
                notificationService.notifyAppointmentReminder(appointment, reminderMinutes, false); // Student
                notificationService.notifyAppointmentReminder(appointment, reminderMinutes, true);  // Staff
            }
        }
    }

    private boolean shouldSendReminder(Appointment appointment) {
        List<Notifications> existingReminders = notificationRepository
                .findByAppointment_AppointmentIdAndActionType(
                        appointment.getAppointmentId(),
                        "APPOINTMENT_REMINDER"
                );

        if (existingReminders.isEmpty()) {
            return true;
        }

        boolean studentHasReminder = existingReminders.stream()
                .anyMatch(n -> n.getUser().getUserId().equals(
                        appointment.getStudent().getUser().getUserId()
                ));

        boolean counselorHasReminder = existingReminders.stream()
                .anyMatch(n -> n.getUser().getUserId().equals(
                        appointment.getGuidanceStaff().getUser().getUserId()
                ));

        return !(studentHasReminder && counselorHasReminder);
    }

    private LocalDateTime extractRescheduleRequestTime(Appointment appointment) {
        if (appointment.getNotes() == null) return null;

        String[] lines = appointment.getNotes().split("\n");
        for (String line : lines) {
            if (line.startsWith("RESCHEDULE REQUEST|")) {
                String[] parts = line.split("\\|");
                if (parts.length > 1) {
                    try {
                        return LocalDateTime.parse(parts[1]);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to parse reschedule timestamp", e);
                    }
                }
            }
        }
        return null;
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

    private void markOldNotificationsAsRead(Appointment appointment, String actionType) {
        List<Notifications> oldNotifications = notificationRepository
                .findByAppointment_AppointmentIdAndActionType(
                        appointment.getAppointmentId(),
                        actionType
                );

        if (!oldNotifications.isEmpty()) {
            LocalDateTime now = AppointmentMessageBuilder.nowUTC();
            oldNotifications.forEach(n -> {
                n.setIsRead(1);
                n.setUpdatedAt(now);
            });
            notificationRepository.saveAll(oldNotifications);
        }
    }
}