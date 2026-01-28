package org.rocs.asa.repository.user;

import org.rocs.asa.domain.section.Section;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * {@code PersonRepository} is an interface of User repository
 * */
public interface UserRepository extends JpaRepository<User,Long> {
    /**
     * Finds a user by their username.
     *
     * @param username username of the user
     * @return username
     */
    User findUserByUsername(String username);
    /**
     * Finds a user by their username.
     *
     * @param email email of the user
     * @return email
     */
    User findUserByPersonEmail(String email);

    User findByUserId(String userId);

    User findByRole(String role);

    List<User> findAllByRole(String role);

    User findUserByPersonContactNumber(String contactNumber);

    @Query("SELECT u.userId FROM Student s JOIN s.user u WHERE s.section.sectionName = :sectionName AND u.role = :role")
    List<String> findUserIdsBySectionNameAndRole(@Param("sectionName") String sectionName, @Param("role") String role);

    List<User> findByUserIdIn(Set<String> userIds);
}
