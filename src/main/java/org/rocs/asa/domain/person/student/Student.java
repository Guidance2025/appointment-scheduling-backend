package org.rocs.asa.domain.person.student;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.section.Section;

@Entity
@Data
@Table(name = "tbl_student")
public class Student extends Person{

    @Column(name = "student_number",nullable = false)
    private String studentNumber;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "section_id")
    private Section section;
}