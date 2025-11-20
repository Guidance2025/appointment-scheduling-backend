package org.rocs.asa.domain.password.reset;

import lombok.Data;

/**
 * Data Transfer Object for password reset requests
 */
@Data
public class PasswordResetRequest {

    private String username;
    private String newPassword;
}