package org.rocs.asa.domain.account.profile.request;

import lombok.Data;

@Data
public class CounselorProfileDto {
    private String positionInRc;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String contactNumber;
}
