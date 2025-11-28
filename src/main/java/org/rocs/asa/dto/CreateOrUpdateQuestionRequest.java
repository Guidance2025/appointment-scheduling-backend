package org.rocs.asa.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateQuestionRequest(
        @NotBlank String questionText,
        Long employeeNumber
) {}
