package org.rocs.asa.service.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.rocs.asa.domain.exit.request.ExitInterviewRequest;
import org.rocs.asa.domain.questions.Questions;

import java.util.List;

public interface ExitInterviewService {
    List<Questions> createMultipleExitInterviewQuestions(Long guidanceStaffId, List<String> questionText, String categoryName);
    List<Questions> findByGuidanceStaffId(Long guidanceStaffId);
    List<Questions> findAllQuestions();
    ExitInterview studentResponse(ExitInterviewRequest request);
    List<ExitInterview> retrieveStudentResponse();
    List<Questions> getUnansweredQuestionsForAuthenticatedStudent();
}