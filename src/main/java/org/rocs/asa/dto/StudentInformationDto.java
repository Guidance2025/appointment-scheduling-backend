package org.rocs.asa.dto;

import lombok.Data;

@Data
public class StudentInformationDto {
    String StudentNumber;
    String firstname;
    String lastName;

    public StudentInformationDto(String studentNumber, String firstname, String lastName) {
        StudentNumber = studentNumber;
        this.firstname = firstname;
        this.lastName = lastName;
    }
}
