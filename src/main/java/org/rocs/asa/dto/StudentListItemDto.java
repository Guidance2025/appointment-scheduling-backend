package org.rocs.asa.dto;

public record StudentListItemDto(
        Long student_id,
        String name,
        String student_number,
        String course,
        String cluster_name,
        boolean has_response
) {}
