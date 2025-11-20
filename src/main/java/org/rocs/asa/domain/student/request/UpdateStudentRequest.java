package org.rocs.asa.domain.student.request;

import lombok.Data;

@Data
public class UpdateStudentRequest {
    String studentNumber;
    String newPassword;
    Boolean isLocked;
}
