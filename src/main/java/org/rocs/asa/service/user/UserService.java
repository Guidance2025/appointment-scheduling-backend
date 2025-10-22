package org.rocs.asa.service.user;

import jakarta.mail.MessagingException;
import org.rocs.asa.domain.registration.Registration;
import org.rocs.asa.domain.user.User;

import java.util.Map;

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
     * @param registration is the data transfer object of the user that contains the user credential
     * @return User
     */
    Registration registerUser(Registration registration);
    /**
     * this is used to set a new password when a user forgets it
     *
     * @param user is the object that contains the user credentials
     * */
    User forgetPassword(User user) throws MessagingException;

    Map<String ,Object> buildLoginResponse(User user);
}