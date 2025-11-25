package org.rocs.asa.domain.student.information.response;

import lombok.Data;

@Data
public class StudentInfoResponse {
    String StudentNumber;
    String firstname;
    String lastName;

    public StudentInfoResponse(String studentNumber, String firstname, String lastName) {
        StudentNumber = studentNumber;
        this.firstname = firstname;
        this.lastName = lastName;
    }
}
