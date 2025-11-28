package org.rocs.asa.dto;

public record AnswerDto(
        Long question_id,
        String question_text,
        String response_text
) {}
