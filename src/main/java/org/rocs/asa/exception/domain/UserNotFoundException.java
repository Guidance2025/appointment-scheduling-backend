package org.rocs.asa.exception.domain;
/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
