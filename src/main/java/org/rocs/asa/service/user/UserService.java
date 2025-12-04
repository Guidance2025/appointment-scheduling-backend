package org.rocs.asa.service.user;

import jakarta.mail.MessagingException;
import org.rocs.asa.domain.registration.Registration;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.service.password.reset.PasswordResetTokenService;

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
    Registration registerUser(Registration registration) throws MessagingException;

    /**
     * Initiates password reset process
     * Sends verification email with token
     *
     * @param username user's username
     * @param newPassword the new password to set after verification
     */
    void initiatePasswordReset(String username, String newPassword) throws MessagingException;

    /**
     * Verifies token and completes password reset
     *
     * @param token verification token from email
     */
    void verifyAndCompletePasswordReset(String token);
    /**
     * this is used to set a new password when a user forgets it
     *
     * @param user is the object that contains the user credentials
     * */

    Map<String ,Object> buildLoginResponse(User user);
}