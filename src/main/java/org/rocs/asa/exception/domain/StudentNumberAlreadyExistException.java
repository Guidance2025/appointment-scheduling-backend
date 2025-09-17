package org.rocs.asa.exception.domain;
/**
 * Exception thrown when attempting to create a Student with a student number
 * that already exists in the system.
 */
public class StudentNumberAlreadyExistException extends RuntimeException {
    /**
     * Constructs a new StudentNumberAlreadyExistException with the specified detail message.
     *
     * @param message the detail message indicating the student number conflict
     */
    public StudentNumberAlreadyExistException(String message) {
        super(message);
    }
}
