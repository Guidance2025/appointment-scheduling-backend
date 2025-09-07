package org.rocs.asa.service.user;

import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@code UserService} is an interface of the UserService
 * */
public interface UserService{
    /**
     * find the user by their username
     *
     * @param username is the username provided by the user
     * @return User
     */
    User findUserByUsername(String username);

    /**
     * find the user by their email
     *
     * @param email is the email provided by the user
     * @return User
     */
    User findUserByPersonEmail(String email);

    /**
     * registers the user using their credentials
     *
     * @param user is the object of the user that contains the user credential
     * @return User
     */
    User registerUser(User user);
}