package org.rocs.asa.domain.student.request;

import lombok.Data;

@Data
public class UpdateStudentProfileRequest {
    String email;
    String contactNumber;
    String address;
}
