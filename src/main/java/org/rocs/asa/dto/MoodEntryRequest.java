package org.rocs.asa.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class MoodEntryRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotEmpty(message = "At least one emotion is required")
    private List<String> emotions;

    @Size(max = 128, message = "Note cannot exceed 128 characters")
    private String note;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public List<String> getEmotions() { return emotions; }
    public void setEmotions(List<String> emotions) { this.emotions = emotions; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}