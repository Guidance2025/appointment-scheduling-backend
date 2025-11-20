package org.rocs.asa.service.student.profile;

import org.rocs.asa.domain.student.information.response.StudentInformationDto;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.dto.StudentInformationDto;
import java.util.List;

public interface StudentProfileService {

    Student saveStudentProfile(Student student);
    StudentInformationDto getPersonByStudentNumber (String studentNumber);
}
