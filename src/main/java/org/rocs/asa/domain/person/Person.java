package org.rocs.asa.domain.person;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Data
@Table(name = "tbl_person")
public class Person implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Firstname is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Middlename is required")
    @Column(nullable = false)
    private String middleName;

    @NotBlank(message = "Lastname is required")
    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Integer age;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private Date birthdate;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contactNumber;

    @OneToOne(mappedBy = "person")
    @JsonIgnore
    private GuidanceStaff guidanceStaff;

    @PrePersist
    @PreUpdate
    public void calculateAge() {
        if (this.birthdate != null) {
            LocalDate birthLocalDate = this.birthdate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate now = LocalDate.now();
            this.age = Period.between(birthLocalDate, now).getYears();
        } else {
            this.age = 0;
        }
    }
}