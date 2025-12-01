package org.rocs.asa.dto.exit;

public class SubmitAnswerRequest {
    private Long questionId;
    private String responseText;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
}