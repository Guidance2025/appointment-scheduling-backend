package org.rocs.asa.service.appointment.helper;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;

import java.time.format.DateTimeFormatter;

public class AppointmentMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    public static String forStudent(GuidanceStaff staff, Appointment appointment) {
        String counselorName = staff.getPerson().getFirstName() + " " + staff.getPerson().getLastName();
        String date = appointment.getScheduledDate().format(DATE_FORMAT);
        String startTime = appointment.getScheduledDate().format(TIME_FORMAT);
        String endTime = appointment.getEndDate().format(TIME_FORMAT);

        return String.format("Appointment with %s on %s (%s - %s)", counselorName, date, startTime, endTime);
    }

    public static String forGuidanceResponse(String studentName, String action, Appointment appointment) {
        String date = appointment.getScheduledDate().format(DATE_FORMAT);
        String startTime = appointment.getScheduledDate().format(TIME_FORMAT);
        String endTime = appointment.getEndDate().format(TIME_FORMAT);

        return String.format("%s has %s your appointment request scheduled at %s (%s - %s)",
                studentName,
                action,
                date,
                startTime,
                endTime
        );
    }
}
