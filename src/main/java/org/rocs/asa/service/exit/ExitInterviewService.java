package org.rocs.asa.service.exit;

import org.rocs.asa.domain.exit.interview.ExitInterviewQuestion;
import org.rocs.asa.domain.exit.interview.ExitInterviewResponse;
import org.rocs.asa.dto.exit.ExitInterviewDetailDto;
import org.rocs.asa.dto.exit.ExitInterviewQuestionRequest;
import org.rocs.asa.dto.exit.ExitInterviewResponseRequest;
import org.rocs.asa.dto.exit.StudentListRow;

import java.util.List;

public interface ExitInterviewService {
    List<StudentListRow> getStudents(String course, String cluster);
    ExitInterviewDetailDto getStudentDetail(Long studentId);
    void saveAnswer(Long studentId, Long questionId, String responseText);

    List<ExitInterviewQuestion> createMultipleExitInterviewQuestions(Long guidanceStaffId, List<String> questionTexts);
    List<ExitInterviewQuestion> findByGuidanceStaffId(Long guidanceStaffId);
    List<ExitInterviewQuestion> findAllQuestions();
    ExitInterviewResponse studentResponse(ExitInterviewResponseRequest request);
    List<ExitInterviewResponse> retrieveStudentResponse();
    List<ExitInterviewQuestion> getUnansweredQuestionsForAuthenticatedStudent();

    ExitInterviewQuestion createQuestion(Long staffId, ExitInterviewQuestionRequest request);
    ExitInterviewQuestion updateQuestion(Long id, ExitInterviewQuestionRequest request);
    void deleteQuestion(Long id);
    List<ExitInterviewResponse> getResponses();
    ExitInterviewResponse submitResponse(Long studentId, ExitInterviewResponseRequest request);

    List<ExitInterviewQuestion> getQuestions(Long staffId);
}