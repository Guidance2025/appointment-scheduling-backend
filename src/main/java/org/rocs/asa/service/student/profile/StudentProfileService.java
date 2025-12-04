package org.rocs.asa.service.student.profile;

import org.rocs.asa.domain.student.information.response.StudentInfoResponse;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.student.request.UpdateStudentProfileRequest;
import org.rocs.asa.domain.student.request.UpdateStudentRequest;

public interface StudentProfileService {

    Student saveStudentProfile(Student student);
    StudentInfoResponse getPersonByStudentNumber (String studentNumber);
    Student getStudentProfile(Long id);
    Student updateStudentProfile(Long id , UpdateStudentProfileRequest request);
}
