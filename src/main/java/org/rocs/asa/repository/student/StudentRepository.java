package org.rocs.asa.repository.student;

import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    Student findStudentByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String StudentNumber);
    Student findByUser (User user);
    List<Student> findTop10ByStudentNumberStartingWithIgnoreCase(String studentNumber);
    @Query("SELECT s FROM Student s JOIN FETCH s.person JOIN FETCH s.section WHERE s.person IS NOT NULL AND s.section IS NOT NULL")
    List<Student> findStudentsWithValidData();
//    List<StudentListItemDto> listStudentsWithHasResponse(String course, String cluster);
//    Optional<StudentInfoDto> findStudentInfo(Long studentId);
}
