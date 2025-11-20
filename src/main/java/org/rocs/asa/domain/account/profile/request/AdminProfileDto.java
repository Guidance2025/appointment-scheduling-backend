package org.rocs.asa.domain.account.profile.request;

import lombok.Data;

@Data
public class AdminProfileDto {
    private String firstname;
    private String lastname;
    private String email;
}
