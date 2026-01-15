package org.rocs.asa.service.appointment.message;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentMessageBuilder {

    private static final ZoneId PH_ZONE = ZoneId.of("Asia/Manila");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy");

    /** Current time in PH timezone */
    public static LocalDateTime nowPH() {
        return LocalDateTime.now(PH_ZONE);
    }

    /** Current time in UTC timezone */
    public static LocalDateTime nowUTC() {
        return LocalDateTime.now(UTC_ZONE);
    }

    /** Convert UTC time to PH timezone */
    public static LocalDateTime toPH(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return null;
        return utcDateTime.atZone(UTC_ZONE)
                .withZoneSameInstant(PH_ZONE)
                .toLocalDateTime();
    }

    /** Convert PH time to UTC for storage */
    public static LocalDateTime toUTC(LocalDateTime phDateTime) {
        if (phDateTime == null) return null;
        return phDateTime.atZone(PH_ZONE)
                .withZoneSameInstant(UTC_ZONE)
                .toLocalDateTime();
    }

    /** Format full datetime for display in PH timezone */
    public static String formatFullDateTimePH(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return "N/A";
        ZonedDateTime phTime = utcDateTime.atZone(UTC_ZONE).withZoneSameInstant(PH_ZONE);
        return phTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"));
    }

    /** Format date only */
    public static String formatDatePH(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return "N/A";
        ZonedDateTime phTime = utcDateTime.atZone(UTC_ZONE).withZoneSameInstant(PH_ZONE);
        return phTime.format(DATE_FORMATTER);
    }

    /** Format time only */
    public static String formatTimePH(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return "N/A";
        ZonedDateTime phTime = utcDateTime.atZone(UTC_ZONE).withZoneSameInstant(PH_ZONE);
        return phTime.format(TIME_FORMATTER);
    }

    /** Format time range */
    public static String formatTimeRange(LocalDateTime startUTC, LocalDateTime endUTC) {
        return formatTimePH(startUTC) + " - " + formatTimePH(endUTC);
    }

    /** Message for appointment response (accept/decline) */
    public static String forGuidanceResponse(String responderName, String action, Appointment appointment) {
        String actionText = "ACCEPT".equalsIgnoreCase(action) ? "accepted" : "declined";
        return String.format("%s has %s the appointment scheduled for %s at %s",
                responderName,
                actionText,
                formatDatePH(appointment.getScheduledDate()),
                formatTimeRange(appointment.getScheduledDate(), appointment.getEndDate())
        );
    }

    /** Update notification message */
    public static String forUpdate(GuidanceStaff staff, Appointment appointment) {
        String staffName = staff.getPerson().getFirstName() + " " + staff.getPerson().getLastName();
        return String.format("%s has rescheduled your appointment to %s at %s",
                staffName,
                formatDatePH(appointment.getScheduledDate()),
                formatTimeRange(appointment.getScheduledDate(), appointment.getEndDate())
        );
    }
}
