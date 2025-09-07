package org.rocs.asa.domain.guidance.staff;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.person.Person;

@Data
@Entity
@Table(name = "tbl_guidance_staff")
public class GuidanceStaff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_number")
    private Long employeeNumber;

    @OneToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "position_in_rc")
    private String positionInRc;

}
