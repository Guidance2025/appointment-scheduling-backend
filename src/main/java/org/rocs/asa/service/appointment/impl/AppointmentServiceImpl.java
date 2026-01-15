package org.rocs.asa.service.appointment.impl;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.appointment.request.RescheduleAppointmentRequest;
import org.rocs.asa.domain.appointment.request.UpdateAppointmentRequest;
import org.rocs.asa.domain.appointment.response.BookedSlotsResponse;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.exception.domain.AppointmentNotFoundException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.service.appointment.AppointmentService;
import org.rocs.asa.service.appointment.availability.AppointmentAvailabilityBlockService;
import org.rocs.asa.service.appointment.creation.AppointmentCreationService;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.rocs.asa.service.appointment.notification.AppointmentNotificationService;
import org.rocs.asa.service.appointment.reschedule.AppointmentRescheduleService;
import org.rocs.asa.service.appointment.response.AppointmentResponseService;
import org.rocs.asa.service.appointment.status.AppointmentStatusService;
import org.rocs.asa.service.appointment.validator.AppointmentValidator;
import org.rocs.asa.service.guidance.GuidanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main appointment service - orchestrates specialized services
 * Reduced from 1000+ lines to ~200 lines
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentCreationService creationService;
    private final AppointmentRescheduleService rescheduleService;
    private final AppointmentStatusService statusService;
    private final AppointmentResponseService responseService;
    private final AppointmentAvailabilityBlockService blockService;
    private final AppointmentNotificationService notificationService;
    private final AppointmentValidator validator;
    private final GuidanceService guidanceService;

    @Autowired
    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            AppointmentCreationService creationService,
            AppointmentRescheduleService rescheduleService,
            AppointmentStatusService statusService,
            AppointmentResponseService responseService,
            AppointmentAvailabilityBlockService blockService,
            AppointmentNotificationService notificationService,
            AppointmentValidator validator,
            GuidanceService guidanceService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.creationService = creationService;
        this.rescheduleService = rescheduleService;
        this.statusService = statusService;
        this.responseService = responseService;
        this.blockService = blockService;
        this.notificationService = notificationService;
        this.validator = validator;
        this.guidanceService = guidanceService;
    }

    @Override
    @Transactional
    public Appointment createAppointment(Appointment request) {
        return creationService.createByStaff(request);
    }

    @Override
    @Transactional
    public Appointment studentCreateAppointment(Appointment request) {
        return creationService.createByStudent(request);
    }

    @Override
    @Transactional
    public Appointment guidanceStaffResponse(Long appointmentId, Map<String, String> data) {
        return responseService.staffRespond(appointmentId, data);
    }

    @Override
    @Transactional
    public Appointment studentResponseToAppointment(Long appointmentId, Map<String, String> data) {
        return responseService.studentRespond(appointmentId, data);
    }

    @Override
    @Transactional
    public Appointment studentRescheduleAppointment(RescheduleAppointmentRequest request) {
        return rescheduleService.requestReschedule(request);
    }

    @Override
    @Transactional
    public Appointment rescheduleAppointmentResponse(Long appointmentId, Map<String, String> data) {
        return rescheduleService.respondToReschedule(appointmentId, data);
    }

    @Override
    @Transactional
    public Appointment counselorResponseToReschedule(Long appointmentId, Map<String, String> data) {
        return rescheduleService.respondToReschedule(appointmentId, data);
    }

    @Override
    @Transactional
    public Appointment updateCounselorAppointment(UpdateAppointmentRequest request) {
        LOGGER.info("Updating appointment ID: {}", request.getAppointmentId());

        Appointment appointment = findAppointmentById(request.getAppointmentId());

        LocalDateTime scheduledUTC = AppointmentMessageBuilder.toUTC(request.getScheduledDate());
        LocalDateTime endUTC = AppointmentMessageBuilder.toUTC(request.getEndDate());

        validator.validateAppointmentDates(scheduledUTC, endUTC);

        boolean datesChanged = !appointment.getScheduledDate().equals(scheduledUTC) ||
                !appointment.getEndDate().equals(endUTC);

        if (datesChanged) {
            validator.validateGuidanceStaffAvailabilityExcluding(
                    appointment.getGuidanceStaff().getId(),
                    scheduledUTC,
                    endUTC,
                    appointment.getAppointmentId()
            );
        }

        appointment.setScheduledDate(scheduledUTC);
        appointment.setEndDate(endUTC);
        Appointment saved = appointmentRepository.save(appointment);

        if (datesChanged) {
            String message = AppointmentMessageBuilder.forUpdate(
                    appointment.getGuidanceStaff(),
                    saved
            );
            notificationService.notifyAppointmentUpdate(saved, message);
        }

        LOGGER.info("Appointment updated successfully. ID: {}", saved.getAppointmentId());
        return saved;
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);

        if (AppointmentStatus.COMPLETED.name().equals(appointment.getStatus()) ||
                "EXPIRED".equals(appointment.getStatus()) ||
                AppointmentStatus.ONGOING.name().equals(appointment.getStatus())) {
            throw new IllegalStateException("Cannot cancel completed or ongoing appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED.name());
        appointmentRepository.save(appointment);

        notificationService.notifyAppointmentCancellation(appointment);
        return true;
    }

    @Override
    @Transactional
    public void markAsOnGoingOrIsCompleted() {
        statusService.updateOngoingAndCompleted();
    }

    @Override
    @Transactional
    public void cleanupExpiredAvailabilityBlocks() {
        statusService.cleanupExpiredAvailabilityBlocks();
    }

    @Override
    @Transactional
    public void expirePendingRequests() {
        statusService.expirePendingRequests();
    }

    @Override
    @Transactional
    public void expireReschedulePendingRequests() {
        statusService.expireReschedulePendingRequests();
    }

    @Override
    public void sendAppointmentReminders() {
        statusService.sendAppointmentReminders();
    }

    @Override
    @Transactional
    public Appointment createAvailabilityBlock(Long staffId, LocalDateTime start,
                                               LocalDateTime end, String reason) {
        return blockService.createBlock(staffId, start, end, reason);
    }

    @Override
    public List<Appointment> getAvailabilityBlocks(Long guidanceStaffId) {
        return blockService.getBlocks(guidanceStaffId);
    }

    @Override
    public List<Appointment> getAvailabilityBlocksInRange(Long guidanceStaffId,
                                                          LocalDateTime startDate,
                                                          LocalDateTime endDate) {
        return blockService.getBlocksInRange(guidanceStaffId, startDate, endDate);
    }

    @Override
    @Transactional
    public Appointment updateAvailabilityBlock(Long blockId, LocalDateTime scheduledDate,
                                               LocalDateTime endDate, String reason) {
        return blockService.updateBlock(blockId, scheduledDate, endDate, reason);
    }

    @Override
    @Transactional
    public boolean deleteAvailabilityBlock(Long blockId) {
        return blockService.deleteBlock(blockId);
    }

    @Override
    @Transactional
    public List<Appointment> createMonthLeave(Long staffId, int year, int month, String reason) {
        return blockService.createMonthLeave(staffId, year, month, reason);
    }

    @Override
    public List<Appointment> getLeaveBlocks(Long guidanceStaffId) {
        return blockService.getLeaveBlocks(guidanceStaffId);
    }

    @Override
    @Transactional
    public int deleteMonthLeave(Long staffId, int year, int month) {
        return blockService.deleteMonthLeave(staffId, year, month);
    }



    @Override
    public List<Appointment> findAppointmentByStatus(Long guidanceStaffId) {
        List<String> statusList = List.of(
                AppointmentStatus.SCHEDULED.name(),
                AppointmentStatus.PENDING.name()
        );
        return appointmentRepository.findByGuidanceStaffAndStatusInExcludingBlocks(
                guidanceStaffId,
                statusList
        );
    }

    @Override
    public Appointment findAppointmentsByAppointmentId(Long appointmentId) {
        return findAppointmentById(appointmentId);
    }

    @Override
    public List<Appointment> getAppointmentByGuidanceStaff(Long guidanceStaffId) {
        return appointmentRepository.findByGuidanceStaffExcludingBlocks(guidanceStaffId);
    }

    @Override
    public List<Appointment> getAppointmentByStudent(Long studentId) {
        return appointmentRepository.findByStudent_Id(studentId);
    }

    @Override
    public List<Appointment> findStudentAppointments(Long studentId, List<String> status) {
        return appointmentRepository.findByStudent_IdAndStatusInIgnoreCase(studentId, status);
    }

    @Override
    public List<BookedSlotsResponse> getBookedSlots(String date) {
        var guidanceStaff = guidanceService.findAuthenticatedGuidanceStaff();
        LocalDate targetDate = LocalDate.parse(date);

        LocalDateTime startOfDayPH = targetDate.atStartOfDay();
        LocalDateTime endOfDayPH = targetDate.atTime(23, 59, 59);

        LocalDateTime startOfDayUTC = AppointmentMessageBuilder.toUTC(startOfDayPH);
        LocalDateTime endOfDayUTC = AppointmentMessageBuilder.toUTC(endOfDayPH);

        List<Appointment> appointments = appointmentRepository
                .findByGuidanceStaff_IdAndScheduledDateBetween(
                        guidanceStaff.getId(),
                        startOfDayUTC,
                        endOfDayUTC
                );

        return appointments.stream()
                .filter(apt -> AppointmentStatus.SCHEDULED.name().equals(apt.getStatus()) ||
                        "BLOCKED".equals(apt.getStatus()))
                .map(apt -> new BookedSlotsResponse(apt.getScheduledDate(), apt.getEndDate()))
                .collect(Collectors.toList());
    }


    private Appointment findAppointmentById(Long appointmentId) {
        if (appointmentId == null) {
            throw new AppointmentNotFoundException("Appointment ID cannot be null");
        }
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));
    }
}