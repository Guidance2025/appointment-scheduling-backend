package org.rocs.asa.service.exit;

import org.rocs.asa.dto.exit.ExitInterviewDetailDto;
import org.rocs.asa.dto.exit.StudentListRow;

import java.util.List;

public interface ExitInterviewService {
    List<StudentListRow> getStudents(String course, String cluster);
    ExitInterviewDetailDto getStudentDetail(Long studentId);
    void saveAnswer(Long studentId, Long questionId, String responseText);
}

