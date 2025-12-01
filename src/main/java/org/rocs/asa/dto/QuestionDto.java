package org.rocs.asa.dto;

import java.time.LocalDate;

public record QuestionDto(
        Long question_id,
        String question_text,
        LocalDate date_created
) {}