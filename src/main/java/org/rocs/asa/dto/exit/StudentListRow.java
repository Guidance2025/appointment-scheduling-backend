package org.rocs.asa.dto.exit;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StudentListRow(
        @JsonProperty("student_id") Long studentId,
        String name,
        @JsonProperty("student_number") String studentNumber,
        String course,
        @JsonProperty("cluster_name") String clusterName,
        @JsonProperty("has_response") boolean hasResponse
) {}
