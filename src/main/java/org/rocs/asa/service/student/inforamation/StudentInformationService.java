package org.rocs.asa.service.student.inforamation;

import org.rocs.asa.domain.student.information.response.StudentDetailsResponse;
import java.util.List;

public interface StudentInformationService {
    List<StudentDetailsResponse> getAllStudent();

}
