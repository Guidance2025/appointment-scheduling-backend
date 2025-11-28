package org.rocs.asa.repository.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection; import java.util.List; import java.util.Optional;

public interface ExitInterviewRepository extends JpaRepository<ExitInterview, Long> {

    List<ExitInterview> findByStudent_IdAndQuestion_Category_CategoryNameIgnoreCaseOrderByQuestion_QuestionIdAsc(
            Long studentId, String categoryName
    );

    Optional<ExitInterview> findFirstByStudent_IdOrderBySubmittedDateDesc(Long studentId);

    void deleteByStudent_IdAndQuestion_Category_CategoryNameIgnoreCase(Long studentId, String categoryName);

    long countByQuestion_QuestionId(Long questionId);

    @Query("""
                select distinct a.student.id
                from ExitInterviewAnswer a
                where upper(a.question.category.categoryName) = upper(:categoryName)
                  and a.student.id in :studentIds
            """)
    List<Long> findDistinctStudentIdsWithCategory(
            @Param("categoryName") String categoryName,
            @Param("studentIds") Collection<Long> studentIds
    );

}

