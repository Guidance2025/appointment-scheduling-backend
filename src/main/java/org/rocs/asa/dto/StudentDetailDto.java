package org.rocs.asa.dto;

import java.time.LocalDate;
import java.util.List;

public record StudentDetailDto(
        StudentInfoDto student,
        LocalDate submittedAt,
        List<AnswerDto> answers
) {}