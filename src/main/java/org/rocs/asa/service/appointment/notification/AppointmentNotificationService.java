package org.rocs.asa.service.appointment.notification;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.rocs.asa.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles all appointment-related notifications
 */
@Service
public class AppointmentNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentNotificationService.class);

    private final NotificationService notificationService;

    public AppointmentNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Notifies student of new appointment request from staff
     */
    public void notifyStudentOfRequest(Appointment appointment) {
        String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());
        String dateTime = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

        String message = String.format(
                "%s has requested an appointment with you at %s. Type: %s",
                staffName,
                dateTime,
                appointment.getAppointmentType()
        );

        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Appointment Request",
                message,
                "APPOINTMENT_REQUEST"
        );
    }

    /**
     * Notifies guidance staff of new appointment request from student
     */
    public void notifyGuidanceStaffOfRequest(Appointment appointment) {
        String studentName = getStudentFullName(appointment.getStudent());
        String dateTime = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

        String message = String.format(
                "%s has requested an appointment with you at %s. Type: %s",
                studentName,
                dateTime,
                appointment.getAppointmentType()
        );

        sendNotification(
                appointment.getGuidanceStaff().getUser().getUserId(),
                appointment.getGuidanceStaff().getUser(),
                appointment,
                "Appointment Request",
                message,
                "APPOINTMENT_REQUEST"
        );
    }

    /**
     * Notifies about appointment response (accept/decline)
     */
    public void notifyAppointmentResponse(Appointment appointment, String action, boolean notifyStaff) {
        String responderName = notifyStaff
                ? getStudentFullName(appointment.getStudent())
                : getGuidanceStaffFullName(appointment.getGuidanceStaff());

        String message = AppointmentMessageBuilder.forGuidanceResponse(
                responderName,
                action,
                appointment
        );

        String recipientUserId = notifyStaff
                ? appointment.getGuidanceStaff().getUser().getUserId()
                : appointment.getStudent().getUser().getUserId();

        var recipient = notifyStaff
                ? appointment.getGuidanceStaff().getUser()
                : appointment.getStudent().getUser();

        sendNotification(
                recipientUserId,
                recipient,
                appointment,
                "Appointment Response",
                message,
                action.toUpperCase()
        );
    }

    /**
     * Notifies about appointment update
     */
    public void notifyAppointmentUpdate(Appointment appointment, String message) {
        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Appointment Update",
                message,
                "APPOINTMENT_UPDATE"
        );
    }

    /**
     * Notifies about appointment cancellation
     */
    public void notifyAppointmentCancellation(Appointment appointment) {
        String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());
        String date = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

        String message = String.format(
                "Your appointment with %s on %s has been cancelled.",
                staffName, date
        );

        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Appointment Cancelled",
                message,
                "APPOINTMENT_CANCELLED"
        );
    }

    /**
     * Notifies about reschedule request
     */
    public void notifyRescheduleRequest(Appointment appointment, String oldTime, String newTime, String reason) {
        String studentName = getStudentFullName(appointment.getStudent());
        String reasonText = reason != null && !reason.isEmpty() ? " Reason: " + reason : "";

        String message = String.format(
                "%s has requested to reschedule from %s to %s.%s Please approve or decline.",
                studentName, oldTime, newTime, reasonText
        );

        sendNotification(
                appointment.getGuidanceStaff().getUser().getUserId(),
                appointment.getGuidanceStaff().getUser(),
                appointment,
                "Reschedule Request",
                message,
                "RESCHEDULE REQUEST"
        );
    }

    /**
     * Notifies about approved reschedule
     */
    public void notifyRescheduleApproved(Appointment appointment, String oldTime, String newTime) {
        String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());

        String message = String.format(
                "%s has approved your reschedule request. Your appointment has been moved from %s to %s.",
                staffName, oldTime, newTime
        );

        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Reschedule Approved",
                message,
                "RESCHEDULE_APPROVED"
        );
    }

    /**
     * Notifies about declined reschedule
     */
    public void notifyRescheduleDeclined(Appointment appointment, String oldTime, String newTime) {
        String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());

        String message = String.format(
                "%s has declined your reschedule request. Your appointment remains at %s.",
                staffName, oldTime
        );

        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Reschedule Declined",
                message,
                "RESCHEDULE_DECLINED"
        );
    }

    /**
     * Notifies about appointment reminder
     */
    public void notifyAppointmentReminder(Appointment appointment, int minutesBefore, boolean toStaff) {
        if (toStaff) {
            String studentName = getStudentFullName(appointment.getStudent());
            String dateTime = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

            String message = String.format(
                    "Reminder: Your appointment with %s is in %d minutes at %s. Type: %s",
                    studentName, minutesBefore, dateTime, appointment.getAppointmentType()
            );

            sendNotification(
                    appointment.getGuidanceStaff().getUser().getUserId(),
                    appointment.getGuidanceStaff().getUser(),
                    appointment,
                    "Appointment Reminder",
                    message,
                    "APPOINTMENT_REMINDER"
            );
        } else {
            String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());
            String dateTime = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

            String message = String.format(
                    "Reminder: Your appointment with %s is in %d minutes at %s. Type: %s",
                    staffName, minutesBefore, dateTime, appointment.getAppointmentType()
            );

            sendNotification(
                    appointment.getStudent().getUser().getUserId(),
                    appointment.getStudent().getUser(),
                    appointment,
                    "Appointment Reminder",
                    message,
                    "APPOINTMENT_REMINDER"
            );
        }
    }

    /**
     * Notifies about expired appointment request
     */
    public void notifyAppointmentExpiration(Appointment appointment) {
        String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());
        String studentName = getStudentFullName(appointment.getStudent());
        String dateTime = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

        String studentMessage = String.format(
                "Your appointment request with %s scheduled for %s has expired due to no response within 5 minutes.",
                staffName, dateTime
        );

        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Appointment Request Expired",
                studentMessage,
                "APPOINTMENT_EXPIRED"
        );

        // Notify staff
        String staffMessage = String.format(
                "Appointment request from %s scheduled for %s has expired due to no response within 5 minutes.",
                studentName, dateTime
        );

        sendNotification(
                appointment.getGuidanceStaff().getUser().getUserId(),
                appointment.getGuidanceStaff().getUser(),
                appointment,
                "Appointment Request Expired",
                staffMessage,
                "APPOINTMENT_EXPIRED"
        );
    }

    /**
     * Notifies about expired reschedule request
     */
    public void notifyRescheduleExpiration(Appointment appointment) {
        String staffName = getGuidanceStaffFullName(appointment.getGuidanceStaff());
        String studentName = getStudentFullName(appointment.getStudent());
        String dateTime = AppointmentMessageBuilder.formatFullDateTimePH(appointment.getScheduledDate());

        // Notify student
        String studentMessage = String.format(
                "Your reschedule request for the appointment with %s on %s has expired. " +
                        "The appointment remains at its original time.",
                staffName, dateTime
        );

        sendNotification(
                appointment.getStudent().getUser().getUserId(),
                appointment.getStudent().getUser(),
                appointment,
                "Reschedule Request Expired",
                studentMessage,
                "RESCHEDULE EXPIRED"
        );

        String staffMessage = String.format(
                "The reschedule request from %s for the appointment on %s has expired. " +
                        "The appointment remains at its original time.",
                studentName, dateTime
        );

        sendNotification(
                appointment.getGuidanceStaff().getUser().getUserId(),
                appointment.getGuidanceStaff().getUser(),
                appointment,
                "Reschedule Request Expired",
                staffMessage,
                "RESCHEDULE EXPIRED"
        );
    }

    private void sendNotification(String userId, org.rocs.asa.domain.user.User user,
                                  Appointment appointment, String title, String message, String actionType) {
        notificationService.sendNotificationToUser(userId, title, message, actionType);
        notificationService.saveNotification(user, appointment, message, actionType);
    }

    private String getStudentFullName(Student student) {
        return student.getPerson().getFirstName() + " " +
                student.getPerson().getLastName();
    }

    private String getGuidanceStaffFullName(GuidanceStaff staff) {
        return staff.getPerson().getFirstName() + " " +
                staff.getPerson().getLastName();
    }
}