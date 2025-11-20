package org.rocs.asa.domain.account.dto;

import lombok.Data;

import java.util.Date;

@Data
public class StudentAccountDto {

    private String studentNumber;
    private String firstname;
    private String middlename;
    private String lastname;
    private String username;
    private String password;
    private Date joinDate;
    private Boolean isActive;
    private Boolean isLocked;
    private String role;
}
