package org.rocs.asa.domain.exit.request;

import lombok.Data;

@Data
public class ExitInterviewRequest {
    private Long studentId;
    private Long questionId;
    private String responseText;
}