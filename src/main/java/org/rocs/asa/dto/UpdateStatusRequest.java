package org.rocs.asa.dto;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private String status;
    private String reason;
    private Long targetUserId; // User to notify about status change
}
