package org.rocs.asa.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SubmitExitInterviewRequest(
        @NotNull Long studentId,
        @NotNull List<SubmitAnswerItem> answers
) {
    public record SubmitAnswerItem(@NotNull Long questionId, String responseText) {}
}
