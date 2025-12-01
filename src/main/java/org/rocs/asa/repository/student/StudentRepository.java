package org.rocs.asa.repository.student;

import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    Student findStudentByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String StudentNumber);
    Student findByUser (User user);

}
