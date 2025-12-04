package org.rocs.asa.repository.exit.interview;

import org.rocs.asa.domain.exit.interview.ExitInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitInterviewRepository extends JpaRepository<ExitInterview, Long> {
    List<ExitInterview> findAllByStudent_IdOrderBySubmittedDateDesc(Long studentId);
}
