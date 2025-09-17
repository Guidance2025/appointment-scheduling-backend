package org.rocs.asa.domain.student;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.section.Section;

@Entity
@Data
@Table(name = "tbl_student")
public class Student{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "student_number",nullable = false)
    private String studentNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    private Person person;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "section_id")
    private Section section;
}