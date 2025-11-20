package org.rocs.asa.domain.student.information.response;

import lombok.Data;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.section.Section;

@Data
public class StudentInformation {
    private String studentNumber;
    private Person person;
    private Section section;
}
