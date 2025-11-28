package org.rocs.asa.dto;

public record StudentInfoDto(
        Long student_id,
        String name,
        String student_number,
        String course,
        String cluster_name
) {}
