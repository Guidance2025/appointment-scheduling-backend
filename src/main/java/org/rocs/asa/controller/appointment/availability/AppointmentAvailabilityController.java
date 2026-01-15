package org.rocs.asa.controller.appointment.availability;

import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.service.appointment.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/counselor/availability")
public class AppointmentAvailabilityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentAvailabilityController.class);

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentAvailabilityController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Create an availability block (full day or partial)
     * POST /counselor/availability/block
     *
     * Request body:
     * {
     *   "guidanceStaffId": 1,
     *   "scheduledDate": "2025-01-15T09:00:00",  // PH time
     *   "endDate": "2025-01-15T12:00:00",        // PH time, null for full day
     *   "reason": "Meeting"
     * }
     */
    @PostMapping("/block")
    public ResponseEntity<?> createAvailabilityBlock(@RequestBody Map<String, Object> request) {
        try {
            Long staffId = Long.parseLong(request.get("guidanceStaffId").toString());
            LocalDateTime scheduledDate = LocalDateTime.parse(request.get("scheduledDate").toString());
            LocalDateTime endDate = request.get("endDate") != null
                    ? LocalDateTime.parse(request.get("endDate").toString())
                    : null;
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";

            Appointment block = appointmentService.createAvailabilityBlock(
                    staffId,
                    scheduledDate,
                    endDate,
                    reason
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Availability block created successfully");
            response.put("block", block);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            LOGGER.error("Error creating availability block: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get all availability blocks for a guidance staff
     * GET /counselor/availability/blocks/{staffId}
     */
    @GetMapping("/blocks/{staffId}")
    public ResponseEntity<?> getAvailabilityBlocks(@PathVariable Long staffId) {
        try {
            List<Appointment> blocks = appointmentService.getAvailabilityBlocks(staffId);
            return ResponseEntity.ok(blocks);

        } catch (Exception e) {
            LOGGER.error("Error fetching availability blocks: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get availability blocks in a date range
     * GET /counselor/availability/blocks/{staffId}/range?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59
     */
    @GetMapping("/blocks/{staffId}/range")
    public ResponseEntity<?> getAvailabilityBlocksInRange(
            @PathVariable Long staffId,
            @RequestParam String start,
            @RequestParam String end
    ) {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);

            List<Appointment> blocks = appointmentService.getAvailabilityBlocksInRange(
                    staffId,
                    startDate,
                    endDate
            );
            return ResponseEntity.ok(blocks);
        }

    /**
     * Update an availability block
     * PUT /counselor/availability/block/{blockId}
     *
     * Request body:
     * {
     *   "scheduledDate": "2025-01-15T10:00:00",  // PH time
     *   "endDate": "2025-01-15T13:00:00",        // PH time, null for full day
     *   "reason": "Updated meeting"
     * }
     */
    @PutMapping("/block/{blockId}")
    public ResponseEntity<?> updateAvailabilityBlock(
            @PathVariable Long blockId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            LocalDateTime scheduledDate = LocalDateTime.parse(request.get("scheduledDate").toString());
            LocalDateTime endDate = request.get("endDate") != null
                    ? LocalDateTime.parse(request.get("endDate").toString())
                    : null;
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";

            Appointment updatedBlock = appointmentService.updateAvailabilityBlock(
                    blockId,
                    scheduledDate,
                    endDate,
                    reason
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Availability block updated successfully");
            response.put("block", updatedBlock);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.error("Error updating availability block: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete an availability block
     * DELETE /counselor/availability/block/{blockId}
     */
    @DeleteMapping("/block/{blockId}")
    public ResponseEntity<?> deleteAvailabilityBlock(@PathVariable Long blockId) {
        try {
            boolean deleted = appointmentService.deleteAvailabilityBlock(blockId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", "Availability block deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.error("Error deleting availability block: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Create a month leave (blocks all working days in a month)
     * POST /counselor/availability/month-leave
     *
     * Request body:
     * {
     *   "guidanceStaffId": 1,
     *   "year": 2025,
     *   "month": 6,  // 1-12
     *   "reason": "Summer vacation"
     * }
     */
    @PostMapping("/month-leave")
    public ResponseEntity<?> createMonthLeave(@RequestBody Map<String, Object> request) {
        try {
            Long staffId = Long.parseLong(request.get("guidanceStaffId").toString());
            int year = Integer.parseInt(request.get("year").toString());
            int month = Integer.parseInt(request.get("month").toString());
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";

            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Month must be between 1 and 12");
            }

            List<Appointment> blocks = appointmentService.createMonthLeave(staffId, year, month, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Month leave created successfully");
            response.put("blockedDays", blocks.size());
            response.put("blocks", blocks);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            LOGGER.error("Error creating month leave: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete a month leave
     * DELETE /counselor/availability/month-leave?guidanceStaffId=1&year=2025&month=6
     */
    @DeleteMapping("/month-leave")
    public ResponseEntity<?> deleteMonthLeave(
            @RequestParam Long guidanceStaffId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        try {
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Month must be between 1 and 12");
            }

            int deletedCount = appointmentService.deleteMonthLeave(guidanceStaffId, year, month);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Month leave deleted successfully");
            response.put("deletedBlocks", deletedCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.error("Error deleting month leave: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get all leave blocks (month leaves and bulk blocks)
     * GET /counselor/availability/leaves/{staffId}
     */
    @GetMapping("/leaves/{staffId}")
    public ResponseEntity<?> getLeaveBlocks(@PathVariable Long staffId) {
        try {
            List<Appointment> leaves = appointmentService.getLeaveBlocks(staffId);
            return ResponseEntity.ok(leaves);

        } catch (Exception e) {
            LOGGER.error("Error fetching leave blocks: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}