package org.rocs.asa.dto.exit;

import lombok.Data;

@Data
public class ExitInterviewResponseRequest {
    private Long questionId;
    private String responseText;
}