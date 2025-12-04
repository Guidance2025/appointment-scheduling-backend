package org.rocs.asa.repository.person;

import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person,Long> {

    List<Person> findByEmail(String email);
}
