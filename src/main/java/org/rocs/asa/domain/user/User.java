package org.rocs.asa.domain.user;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.utils.converter.StringListConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity(name = "tbl_login")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_id",nullable = false, updatable = false)
    private Long loginId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    private Person person;

    private String username;
    private String password;
    //
    @Column(name = "user_id" ,length = 10)
    private String userId;
    //
    private Date joinDate;
    private Date lastLoginDate;
    private String role;

    @Column(name =  "authorities" , nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> authorities;
    private boolean isLocked;
    private boolean isActive;







}
