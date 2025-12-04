package org.rocs.asa.domain.guidance.staff.request;

import lombok.Data;

@Data
public class UpdateGuidanceStaffRequest {
    Long id;
    String email;
    Boolean isLocked;
}
