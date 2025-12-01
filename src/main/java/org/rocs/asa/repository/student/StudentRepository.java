package org.rocs.asa.repository.student;

import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.rocs.asa.dto.StudentInfoDto;
import org.rocs.asa.dto.StudentListItemDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    Student findStudentByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String StudentNumber);
    Student findByUser (User user);
    List<StudentListItemDto> listStudentsWithHasResponse(String course, String cluster);
    Optional<StudentInfoDto> findStudentInfo(Long studentId);

}
