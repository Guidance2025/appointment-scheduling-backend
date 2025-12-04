package org.rocs.asa.dto.question;

public record QuestionDto(
        Long id,
        String text,
        String category
) {}
