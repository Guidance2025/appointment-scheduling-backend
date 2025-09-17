package org.rocs.asa.exception.domain;
/**
 * Exception thrown when an email is not found in the system.
 */
public class EmailNotFoundException extends RuntimeException {
    /**
     * Constructs a new EmailNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public EmailNotFoundException(String message) {
        super(message);
    }
}
