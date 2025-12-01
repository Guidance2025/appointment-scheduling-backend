package org.rocs.asa.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateQuestionRequest(
        @NotBlank String text,
        String category,
        @JsonProperty("employee_number") Long employeeNumber
) {}
