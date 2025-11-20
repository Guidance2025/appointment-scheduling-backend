package org.rocs.asa.domain.student;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.section.Section;
import org.rocs.asa.domain.user.User;

import java.io.Serializable;

@Entity
@Data
@Table(name = "tbl_student")
public class Student implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "student_number",nullable = false)
    private String studentNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "login_id")
    private User user;
}