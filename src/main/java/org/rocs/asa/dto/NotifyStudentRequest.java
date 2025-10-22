package org.rocs.asa.dto;

import lombok.Data;

@Data
public class NotifyStudentRequest {
    private String message;
    private String actionType;
}
