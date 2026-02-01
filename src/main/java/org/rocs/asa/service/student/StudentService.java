package org.rocs.asa.service.student;

import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;

import java.util.List;

public interface StudentService {

    Student findByAuthenticatedStudent();
    Student findByUser(User user);
    List<Student> findBtStudentNumberStartingWith(String studentNumber);
}
