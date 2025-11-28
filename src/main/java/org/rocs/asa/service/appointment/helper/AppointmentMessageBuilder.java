package org.rocs.asa.service.appointment.helper;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId PH = ZoneId.of("Asia/Manila");

    private static ZonedDateTime toPH(ZonedDateTime utcDateTime) {
        return utcDateTime.withZoneSameInstant(PH);
    }

    private static ZonedDateTime toPH(LocalDateTime utcLocalDateTime) {
        return utcLocalDateTime.atZone(UTC).withZoneSameInstant(PH);
    }

    private static String formatDate(LocalDateTime utcDateTime) {
        return toPH(utcDateTime).toLocalDate().format(DATE_FORMAT);
    }

    private static String formatTime(LocalDateTime utcDateTime) {
        return toPH(utcDateTime).toLocalTime().format(TIME_FORMAT);
    }

    // -----------------------------
    // STUDENT VIEW MESSAGE
    // -----------------------------
    public static String forStudent(GuidanceStaff staff, Appointment appointment) {
        String counselorName = staff.getPerson().getFirstName() + " " + staff.getPerson().getLastName();
        String date = formatDate(appointment.getScheduledDate());
        String startTime = formatTime(appointment.getScheduledDate());
        String endTime = formatTime(appointment.getEndDate());

        return String.format("Appointment with %s on %s (%s - %s)",
                counselorName, date, startTime, endTime
        );
    }

    public static String forGuidance(Student student, Appointment appointment) {
        String studentName = student.getPerson().getFirstName() + " " + student.getPerson().getLastName();
        String appointmentType = appointment.getAppointmentType();

        String date = formatDate(appointment.getScheduledDate());
        String startTime = formatTime(appointment.getScheduledDate());
        String endTime = formatTime(appointment.getEndDate());

        return String.format(
                "%s requested a %s appointment on %s from %s to %s.",
                studentName, appointmentType, date, startTime, endTime
        );
    }
    public static String forGuidanceResponse(String studentName, String action, Appointment appointment) {
        String date = formatDate(appointment.getScheduledDate());
        String startTime = formatTime(appointment.getScheduledDate());
        String endTime = formatTime(appointment.getEndDate());

        return String.format(
                "%s has %s your appointment request scheduled - %s (%s - %s)",
                studentName, action, date, startTime, endTime
        );
    }
}
