package org.rocs.asa.service.student;

import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
public interface StudentService {

    Student findByAuthenticatedStudent ();
    Student findByUser(User user);
}
