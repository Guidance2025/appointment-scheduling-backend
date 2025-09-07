package org.rocs.asa.repository.student;

import org.rocs.asa.domain.person.student.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    @EntityGraph(attributePaths = {"section"})
    Student findStudentByStudentNumber(String studentNumber);
}
