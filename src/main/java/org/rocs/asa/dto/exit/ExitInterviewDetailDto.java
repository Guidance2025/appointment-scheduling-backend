package org.rocs.asa.dto.exit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record ExitInterviewDetailDto(
        StudentInfo student,
        @JsonProperty("submitted_date") LocalDateTime submittedDate,
        List<AnswerItem> answers
) {
    public record StudentInfo(
            String name,
            @JsonProperty("student_number") String studentNumber,
            String course,
            @JsonProperty("cluster_name") String clusterName
    ) {}
    public record AnswerItem(
            @JsonProperty("question_text") String questionText,
            @JsonProperty("response_text") String responseText
    ) {}
}
