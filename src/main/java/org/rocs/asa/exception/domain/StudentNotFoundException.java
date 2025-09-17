package org.rocs.asa.exception.domain;
/**
 * Exception thrown when a student is not found.
 */
public class StudentNotFoundException extends RuntimeException {
    /**
     * Constructs a new StudentNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public StudentNotFoundException(String message) {
        super(message);
    }
}
