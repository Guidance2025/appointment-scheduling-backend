package org.rocs.asa.domain.self.request;

import lombok.Data;

@Data
public class SelfAssessmentRequest {
    private Long studentId;
    private Long questionId;
    private String responseText;
}
