package org.rocs.asa.exception.domain;
/**
 * Exception thrown when a username already exists in the system.
 */
public class UsernameExistsException extends RuntimeException {
    /**
     * Constructs a new UsernameExistsException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public UsernameExistsException(String message) {
        super(message);
    }
}
