package org.rocs.asa.repository.questions;

import org.rocs.asa.domain.questions.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionsRepository extends JpaRepository<Questions,Long> {
    List<Questions> findByGuidanceStaffId(Long guidanceStaffId);

    @Query("SELECT q FROM Questions q WHERE q.id NOT IN " +
            "(SELECT sa.question.id FROM SelfAssessment sa WHERE sa.student.id = :studentId)")
    List<Questions> findUnansweredQuestionByStudentId(@Param("studentId") Long studentId);
}
