package org.rocs.asa.repository.questions;

import org.rocs.asa.domain.questions.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionsRepository extends JpaRepository<Questions,Long> {
    List<Questions> findByGuidanceStaffId(Long guidanceStaffId);
    @Query("SELECT q FROM Questions q WHERE q.category.categoryName = :categoryName ORDER BY q.dateCreated DESC")
    List<Questions> findByCategoryName(@Param("categoryName") String categoryName);

    @Query(value = """
    SELECT q.* FROM tbl_questions q
    WHERE q.category_id = (SELECT c.id FROM tbl_categories c WHERE c.category_name = ?)
    AND q.id NOT IN (
        SELECT ei.question_id FROM tbl_exit_interview ei WHERE ei.student_id = ?
    )
    AND (
        q.id IN (
            SELECT CAST(
                SUBSTRING(n.action_type, LENGTH('EXIT_INTERVIEW_NEW_QUESTION_') + 1)
                AS INTEGER
            )
            FROM tbl_notifications n
            WHERE n.user_id = ?
            AND n.action_type LIKE 'EXIT_INTERVIEW_NEW_QUESTION_%'
        )
        OR NOT EXISTS (
            SELECT 1 FROM tbl_notifications n2
            WHERE n2.user_id = ?
            AND n2.action_type LIKE 'EXIT_INTERVIEW_NEW_QUESTION_%'
        )
    )
    """, nativeQuery = true)
    List<Questions> findUnansweredExitInterviewByStudentId (Long studentId, String categoryName,String userId);
    @Query("SELECT q FROM Questions q WHERE q.category.categoryName = :categoryName AND q.id NOT IN (SELECT sa.question.id FROM SelfAssessment sa WHERE sa.student.id = :studentId)")
    List<Questions> findUnansweredSelfAssessmentByStudentId(@Param("studentId") Long studentId, @Param("categoryName") String categoryName);
//    @Query("SELECT q FROM Questions q WHERE q.id NOT IN " +
//            "(SELECT sa.question.id FROM SelfAssessment sa WHERE sa.student.id = :studentId)")
//    List<Questions> findUnansweredQuestionByStudentId(@Param("studentId") Long studentId);
}
