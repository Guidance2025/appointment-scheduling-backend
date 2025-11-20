package org.rocs.asa.domain.notification.status.request;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private String status;
    private String reason;
    private Long targetUserId;
}
