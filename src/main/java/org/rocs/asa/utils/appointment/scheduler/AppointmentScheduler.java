package org.rocs.asa.utils.appointment.scheduler;

import org.rocs.asa.service.appointment.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The {@code AppointmentScheduler} class is responsible for automatically updating
 * appointment statuses at fixed intervals in the system.
 * <p>
 * It ensures appointments are marked as "Ongoing" or "Completed" based on their schedule,
 * without requiring manual intervention.
 */
@Component
public class AppointmentScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentScheduler.class);
    private final AppointmentService appointmentService;

    /**
     * Constructs a new {@code AppointmentScheduler} with the required dependencies.
     * <p>
     * This constructor is annotated with {@code Autowired} to allow Spring to inject
     * the necessary beans at runtime.
     *
     * @param appointmentService the service responsible for managing appointment operations
     */
    @Autowired
    public AppointmentScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * {@code updateAppointmentStatuses} executes a scheduled job every 100 seconds.
     * <p>
     * This method triggers the appointment status evaluation logic, updating
     * appointments to "Ongoing" or "Completed" depending on their current state
     * and scheduled time.
     * <br>
     * Execution time is logged for monitoring purposes, and any exceptions are
     * captured and logged.
     */
    @Scheduled(fixedRate = 100_000)
    public void updateAppointmentStatuses() {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.info("Starting scheduled appointment status update");
            appointmentService.markAsOnGoingOrIsCompleted();

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Appointment status update completed successfully in {}ms", duration);

        } catch (Exception e) {
            LOGGER.error("Failed to update appointment statuses: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends reminder notifications every 5 minutes.
     * Notifies students 30 minutes before their scheduled appointments.
     */
    @Scheduled(fixedRate = 100_000)
    public void sendAppointmentReminders() {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.info("Starting appointment reminder check");
            appointmentService.sendAppointmentReminders();

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Appointment reminder check completed in {}ms", duration);

        } catch (Exception e) {
            LOGGER.error("Failed to send appointment reminders: {}", e.getMessage(), e);
        }
    }

}
