package org.rocs.asa.service.self.assessment;

import org.rocs.asa.domain.questions.Questions;
import org.rocs.asa.domain.self.assesment.SelfAssessment;
import org.rocs.asa.domain.self.request.SelfAssessmentRequest;

import java.util.List;

public interface SelfAssesmentService {
    List<Questions> createMultipleSelfAssessmentQuestions(Long guidanceStaffId, List<String> questionText);
    List<Questions> findByGuidanceStaffId(Long guidanceStaffId);
    List<Questions> findAllQuestions();

    SelfAssessment studentResponse (SelfAssessmentRequest request);

    List <SelfAssessment> retrieveStudentResponse();

    List<Questions> getUnansweredQuestionsForAuthenticatedStudent();
}
