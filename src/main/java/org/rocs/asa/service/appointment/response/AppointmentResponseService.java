package org.rocs.asa.service.appointment.response;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.service.appointment.notification.AppointmentNotificationService;
import org.rocs.asa.service.appointment.validator.AppointmentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Handles appointment response operations (accept/decline)
 */
@Service
public class AppointmentResponseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentResponseService.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentValidator validator;
    private final AppointmentNotificationService notificationService;

    public AppointmentResponseService(
            AppointmentRepository appointmentRepository,
            AppointmentValidator validator,
            AppointmentNotificationService notificationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.validator = validator;
        this.notificationService = notificationService;
    }

    /**
     * Guidance staff responds to appointment request
     */
    @Transactional
    public Appointment staffRespond(Long appointmentId, Map<String, String> data) {
        Appointment appointment = findAppointmentById(appointmentId);
        validator.validateAppointmentIsPending(appointment);

        String action = validator.extractAndValidateAction(data);
        updateStatus(appointment, action);

        Appointment saved = appointmentRepository.save(appointment);
        notificationService.notifyAppointmentResponse(saved, action, false);

        LOGGER.info("Guidance staff {} appointment ID: {}", action, appointmentId);
        return saved;
    }

    /**
     * Student responds to appointment request
     */
    @Transactional
    public Appointment studentRespond(Long appointmentId, Map<String, String> data) {
        Appointment appointment = findAppointmentById(appointmentId);
        validator.validateAppointmentIsPending(appointment);

        String action = validator.extractAndValidateAction(data);
        updateStatus(appointment, action);

        Appointment saved = appointmentRepository.save(appointment);
        notificationService.notifyAppointmentResponse(saved, action, true);

        LOGGER.info("Student {} appointment ID: {}", action, appointmentId);
        return saved;
    }

    private void updateStatus(Appointment appointment, String action) {
        if ("ACCEPT".equals(action)) {
            appointment.setStatus(AppointmentStatus.SCHEDULED.name());
        } else {
            appointment.setStatus(AppointmentStatus.CANCELLED.name());
        }
    }

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));
    }
}