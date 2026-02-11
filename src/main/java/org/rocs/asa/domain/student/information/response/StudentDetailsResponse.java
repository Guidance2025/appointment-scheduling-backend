package org.rocs.asa.domain.student.information.response;

import lombok.Data;
import lombok.Setter;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.section.Section;

@Data
public class StudentDetailsResponse {
    private String studentNumber;
    private Person person;
    private Section section;

    private Long id;
    private String firstName;  // Flat: From Person.firstName
    private String lastName;   // Flat: From Person.lastName
    private String sectionName; // Flat: From Section.sectionName
}
