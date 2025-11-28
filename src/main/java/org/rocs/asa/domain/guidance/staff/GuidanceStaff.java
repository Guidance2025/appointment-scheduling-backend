package org.rocs.asa.domain.guidance.staff;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.user.User;

@Data
@Entity
@Table(name = "tbl_guidance_staff")
public class GuidanceStaff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_number")
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "position_in_rc")
    private String positionInRc;

    @OneToOne
    @JoinColumn(name = "login_id")
    private User user;

    public Long getEmployeeNumber() {
        return this.id;
    }
    public void setEmployeeNumber(Long employeeNumber) {
        this.id = employeeNumber;
    }
}
