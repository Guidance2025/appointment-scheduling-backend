package org.rocs.asa.domain.registration;

import lombok.Data;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.student.Student;

@Data
public class Registration {
    GuidanceStaff guidanceStaff;
    Student student;
}
