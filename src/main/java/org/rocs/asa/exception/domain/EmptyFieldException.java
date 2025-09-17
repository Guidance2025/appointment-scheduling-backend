package org.rocs.asa.exception.domain;
/**
 * Exception thrown when a required field in a Student or related entity is empty or null.
 */
public class EmptyFieldException extends RuntimeException {
    /**
     * Constructs a new EmptyFieldException with the specified detail message.
     *
     * @param message the detail message explaining which field is empty
     */
    public EmptyFieldException(String message) {
        super(message);
    }
}
