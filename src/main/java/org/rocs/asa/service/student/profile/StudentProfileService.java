package org.rocs.asa.service.student.profile;

import org.rocs.asa.domain.student.information.response.StudentInfoResponse;
import org.rocs.asa.domain.student.Student;
public interface StudentProfileService {

    Student saveStudentProfile(Student student);
    StudentInfoResponse getPersonByStudentNumber (String studentNumber);
}
