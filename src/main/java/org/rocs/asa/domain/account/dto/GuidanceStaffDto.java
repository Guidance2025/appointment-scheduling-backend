package org.rocs.asa.domain.account.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GuidanceStaffDto {
    private Long employeeNumber;
    private String firstname;
    private String lastname;
    private String username;
    private String password;
    private Date joinDate;
    private String role;
    private Boolean isActive;
    private Boolean isLocked;
    private String positionInRc;
}
