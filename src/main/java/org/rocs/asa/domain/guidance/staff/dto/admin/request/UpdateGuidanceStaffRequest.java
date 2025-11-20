package org.rocs.asa.domain.guidance.staff.dto.admin.request;

import lombok.Data;

@Data
public class UpdateGuidanceStaffRequest {
    Long id;
    String email;
    Boolean isLocked;
}
