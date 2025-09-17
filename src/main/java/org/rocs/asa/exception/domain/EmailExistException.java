package org.rocs.asa.exception.domain;

/**
 * Exception thrown when an email already exists in the system.
 */
public class EmailExistException extends RuntimeException {
    /**
     * Constructs a new EmailExistException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public EmailExistException(String message) {
        super(message);
    }
}
