package org.rocs.asa.repository.user;

import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

}
