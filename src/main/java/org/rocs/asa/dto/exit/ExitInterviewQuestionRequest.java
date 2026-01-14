package org.rocs.asa.dto.exit;

import lombok.Data;

@Data
public class ExitInterviewQuestionRequest {
    private String questionText;
    private String category;
}