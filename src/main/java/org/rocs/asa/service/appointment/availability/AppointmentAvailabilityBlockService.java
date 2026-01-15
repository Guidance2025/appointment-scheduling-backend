package org.rocs.asa.service.appointment.availability;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.enums.AppointmentStatus;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.exception.domain.AppointmentAlreadyExistException;
import org.rocs.asa.repository.appointment.AppointmentRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.service.appointment.message.AppointmentMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles availability blocking operations
 */
@Service
public class AppointmentAvailabilityBlockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentAvailabilityBlockService.class);
    private static final int WORK_START_HOUR = 8;
    private static final int WORK_END_HOUR = 17;

    private final AppointmentRepository appointmentRepository;
    private final GuidanceStaffRepository guidanceStaffRepository;

    public AppointmentAvailabilityBlockService(
            AppointmentRepository appointmentRepository,
            GuidanceStaffRepository guidanceStaffRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
    }

    /**
     * Creates an availability block (partial or full day)
     */
    @Transactional
    public Appointment createBlock(Long staffId, LocalDateTime start, LocalDateTime end, String reason) {
        if (start == null) {
            throw new IllegalArgumentException("Start date is required");
        }

        boolean isFullDayBlock = (end == null);
        LocalDate targetDate = start.toLocalDate();

        LocalDateTime dayStartPH = targetDate.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEndPH = targetDate.atTime(WORK_END_HOUR, 0);
        LocalDateTime dayStartUTC = AppointmentMessageBuilder.toUTC(dayStartPH);
        LocalDateTime dayEndUTC = AppointmentMessageBuilder.toUTC(dayEndPH);

        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        LocalDateTime startUTC;
        LocalDateTime endUTC = null;

        if (isFullDayBlock) {
            startUTC = dayStartUTC;
            if (startUTC.isBefore(nowUTC)) {
                throw new IllegalArgumentException("Cannot block past dates");
            }
        } else {
            startUTC = AppointmentMessageBuilder.toUTC(start);
            endUTC = AppointmentMessageBuilder.toUTC(end);

            if (startUTC.isBefore(nowUTC)) {
                throw new IllegalArgumentException("Cannot block past dates");
            }

            if (endUTC.isBefore(startUTC)) {
                throw new IllegalArgumentException("End cannot be before start");
            }

            validateBlockingTimes(start, end);
        }

        validateCanBlock(staffId, isFullDayBlock, startUTC, endUTC, dayStartUTC, dayEndUTC);

        GuidanceStaff staff = guidanceStaffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Appointment block = new Appointment();
        block.setGuidanceStaff(staff);
        block.setScheduledDate(startUTC);
        block.setEndDate(endUTC);
        block.setAppointmentType("AVAILABILITY_BLOCK");
        block.setStatus("BLOCKED");
        block.setNotes(reason != null ? reason : "");
        block.setDateCreated(AppointmentMessageBuilder.nowUTC());

        return appointmentRepository.save(block);
    }

    /**
     * Creates month-long leave by blocking all working days
     */
    @Transactional
    public List<Appointment> createMonthLeave(Long staffId, int year, int month, String reason) {
        LOGGER.info("Creating month leave for staff {} - {}/{}", staffId, month, year);

        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        LocalDateTime nowPH = AppointmentMessageBuilder.toPH(nowUTC);

        LocalDate firstDay = LocalDate.of(year, month, 1);
        if (firstDay.isBefore(nowPH.toLocalDate())) {
            throw new IllegalArgumentException("Cannot create leave for past months");
        }

        GuidanceStaff staff = guidanceStaffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        LocalDateTime monthStartUTC = AppointmentMessageBuilder.toUTC(firstDay.atStartOfDay());
        LocalDateTime monthEndUTC = AppointmentMessageBuilder.toUTC(
                firstDay.withDayOfMonth(firstDay.lengthOfMonth()).atTime(23, 59, 59)
        );

        boolean hasAppointments = appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateBetween(
                        staffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        monthStartUTC,
                        monthEndUTC
                );

        if (hasAppointments) {
            throw new AppointmentAlreadyExistException(
                    "Cannot create month leave. You have existing appointments in " +
                            firstDay.getMonth() + " " + year
            );
        }

        List<Appointment> blocks = new ArrayList<>();
        LocalDate currentDate = firstDay;
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        while (!currentDate.isAfter(lastDay)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                if (!currentDate.isBefore(nowPH.toLocalDate())) {
                    LocalDateTime dayStartUTC = AppointmentMessageBuilder.toUTC(
                            currentDate.atTime(WORK_START_HOUR, 0)
                    );

                    boolean alreadyBlocked = appointmentRepository
                            .existsByGuidanceStaffIdAndAppointmentTypeAndStatusAndScheduledDateBetweenAndEndDateIsNull(
                                    staffId,
                                    "AVAILABILITY_BLOCK",
                                    "BLOCKED",
                                    dayStartUTC,
                                    dayStartUTC.plusSeconds(1)
                            );

                    if (!alreadyBlocked) {
                        Appointment block = new Appointment();
                        block.setGuidanceStaff(staff);
                        block.setScheduledDate(dayStartUTC);
                        block.setEndDate(null);
                        block.setAppointmentType("AVAILABILITY_BLOCK");
                        block.setStatus("BLOCKED");
                        block.setNotes("Month Leave: " + (reason != null ? reason : ""));
                        block.setDateCreated(nowUTC);

                        blocks.add(block);
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("No valid working days to block");
        }

        appointmentRepository.saveAll(blocks);
        LOGGER.info("Created {} blocks for month leave", blocks.size());
        return blocks;
    }

    /**
     * Gets all availability blocks
     */
    public List<Appointment> getBlocks(Long guidanceStaffId) {
        return appointmentRepository.findAvailabilityBlocksByGuidanceStaff(guidanceStaffId);
    }

    /**
     * Gets availability blocks in date range
     */
    public List<Appointment> getBlocksInRange(Long guidanceStaffId,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate) {
        LocalDateTime startUTC = AppointmentMessageBuilder.toUTC(startDate);
        LocalDateTime endUTC = AppointmentMessageBuilder.toUTC(endDate);

        return appointmentRepository.findAvailabilityBlocksInDateRange(
                guidanceStaffId,
                startUTC,
                endUTC
        );
    }

    /**
     * Gets leave blocks (full-day blocks)
     */
    public List<Appointment> getLeaveBlocks(Long guidanceStaffId) {
        List<Appointment> allBlocks = appointmentRepository.findAvailabilityBlocksByGuidanceStaff(guidanceStaffId);

        return allBlocks.stream()
                .filter(block -> block.getEndDate() == null)
                .filter(block -> block.getNotes() != null &&
                        (block.getNotes().startsWith("Month Leave:") ||
                                block.getNotes().startsWith("Bulk Block:")))
                .collect(Collectors.toList());
    }

    /**
     * Updates an availability block
     */
    @Transactional
    public Appointment updateBlock(Long blockId, LocalDateTime scheduledDate,
                                   LocalDateTime endDate, String reason) {
        Appointment block = appointmentRepository.findById(blockId)
                .orElseThrow(() -> new RuntimeException("Block not found: " + blockId));

        if (!"AVAILABILITY_BLOCK".equals(block.getAppointmentType())) {
            throw new RuntimeException("Not an availability block");
        }

        LocalDateTime scheduledUTC = AppointmentMessageBuilder.toUTC(scheduledDate);
        LocalDateTime endUTC = endDate != null ? AppointmentMessageBuilder.toUTC(endDate) : null;

        LocalDateTime nowUTC = AppointmentMessageBuilder.nowUTC();
        if (scheduledUTC.isBefore(nowUTC)) {
            throw new IllegalArgumentException("Cannot set block to past dates");
        }

        if (endUTC != null && endUTC.isBefore(scheduledUTC)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        block.setScheduledDate(scheduledUTC);
        block.setEndDate(endUTC);
        block.setNotes(reason != null ? reason : "");

        return appointmentRepository.save(block);
    }

    /**
     * Deletes an availability block
     */
    @Transactional
    public boolean deleteBlock(Long blockId) {
        Appointment block = appointmentRepository.findById(blockId)
                .orElseThrow(() -> new RuntimeException("Block not found: " + blockId));

        if (!"AVAILABILITY_BLOCK".equals(block.getAppointmentType())) {
            throw new RuntimeException("Not an availability block");
        }

        appointmentRepository.delete(block);
        LOGGER.info("Availability block deleted: {}", blockId);
        return true;
    }

    /**
     * Deletes all blocks in a month
     */
    @Transactional
    public int deleteMonthLeave(Long staffId, int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        LocalDateTime startUTC = AppointmentMessageBuilder.toUTC(firstDay.atStartOfDay());
        LocalDateTime endUTC = AppointmentMessageBuilder.toUTC(lastDay.atTime(23, 59, 59));

        List<Appointment> blocksToDelete = appointmentRepository
                .findByGuidanceStaff_IdAndAppointmentTypeAndScheduledDateBetween(
                        staffId,
                        "AVAILABILITY_BLOCK",
                        startUTC,
                        endUTC
                )
                .stream()
                .filter(block -> block.getEndDate() == null)
                .filter(block -> block.getNotes() != null && block.getNotes().startsWith("Month Leave:"))
                .collect(Collectors.toList());

        if (!blocksToDelete.isEmpty()) {
            appointmentRepository.deleteAll(blocksToDelete);
            LOGGER.info("Deleted {} month leave blocks", blocksToDelete.size());
        }

        return blocksToDelete.size();
    }

    /**
     * Checks if a time slot is blocked
     */
    public boolean isTimeSlotBlocked(Long staffId, LocalDateTime startUTC, LocalDateTime endUTC) {
        return appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
                        staffId,
                        List.of(AppointmentStatus.SCHEDULED.name(), "BLOCKED"),
                        endUTC,
                        startUTC
                );
    }

    // Validation helpers
    private void validateCanBlock(Long staffId, boolean isFullDay,
                                  LocalDateTime startUTC, LocalDateTime endUTC,
                                  LocalDateTime dayStartUTC, LocalDateTime dayEndUTC) {
        if (isFullDay) {
            validateCanBlockFullDay(staffId, dayStartUTC, dayEndUTC);
        } else {
            validateCanBlockPartialDay(staffId, startUTC, endUTC);
        }
    }

    private void validateCanBlockFullDay(Long staffId, LocalDateTime dayStartUTC,
                                         LocalDateTime dayEndUTC) {
        boolean fullDayBlocked = appointmentRepository
                .existsByGuidanceStaffIdAndAppointmentTypeAndStatusAndScheduledDateBetweenAndEndDateIsNull(
                        staffId,
                        "AVAILABILITY_BLOCK",
                        "BLOCKED",
                        dayStartUTC,
                        dayEndUTC.plusSeconds(1)
                );

        if (fullDayBlocked) {
            throw new AppointmentAlreadyExistException("This day is already fully blocked");
        }

        boolean hasAppointments = appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateBetween(
                        staffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        dayStartUTC,
                        dayEndUTC.plusSeconds(1)
                );

        if (hasAppointments) {
            throw new AppointmentAlreadyExistException(
                    "Cannot block the whole day because appointments already exist"
            );
        }

        boolean hasPartialBlock = appointmentRepository
                .existsByGuidanceStaffIdAndAppointmentTypeAndStatusAndScheduledDateBetweenAndEndDateIsNotNull(
                        staffId,
                        "AVAILABILITY_BLOCK",
                        "BLOCKED",
                        dayStartUTC,
                        dayEndUTC.plusSeconds(1)
                );

        if (hasPartialBlock) {
            throw new AppointmentAlreadyExistException(
                    "Cannot block the whole day because a time block already exists"
            );
        }
    }

    private void validateCanBlockPartialDay(Long staffId, LocalDateTime startUTC,
                                            LocalDateTime endUTC) {
        boolean hasAppointments = appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
                        staffId,
                        List.of(AppointmentStatus.PENDING.name(), AppointmentStatus.SCHEDULED.name()),
                        endUTC,
                        startUTC
                );

        if (hasAppointments) {
            throw new AppointmentAlreadyExistException(
                    "Cannot block. There is already an appointment in this time range."
            );
        }

        boolean overlapping = appointmentRepository
                .existsByGuidanceStaffIdAndStatusInAndScheduledDateLessThanAndEndDateGreaterThan(
                        staffId,
                        List.of(AppointmentStatus.SCHEDULED.name(), "BLOCKED"),
                        endUTC,
                        startUTC
                );

        if (overlapping) {
            throw new AppointmentAlreadyExistException("This time range is already blocked");
        }
    }

    private void validateBlockingTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null) return;

        int startHour = start.getHour();
        int endHour = end != null ? end.getHour() : 0;
        int endMinute = end != null ? end.getMinute() : 0;

        if (startHour < WORK_START_HOUR) {
            throw new IllegalArgumentException("Blocking can only start from 8:00 AM onwards");
        }

        if (startHour >= WORK_END_HOUR) {
            throw new IllegalArgumentException("Blocking cannot start at or after 5:00 PM");
        }

        if (end != null) {
            if (end.isBefore(start) || end.isEqual(start)) {
                throw new IllegalArgumentException("End time must be after start time");
            }

            if (endHour > WORK_END_HOUR || (endHour == WORK_END_HOUR && endMinute > 0)) {
                throw new IllegalArgumentException("Blocking cannot extend beyond 5:00 PM");
            }

            if (endHour < WORK_START_HOUR) {
                throw new IllegalArgumentException("Blocking cannot end before 8:00 AM");
            }
        }
    }
}