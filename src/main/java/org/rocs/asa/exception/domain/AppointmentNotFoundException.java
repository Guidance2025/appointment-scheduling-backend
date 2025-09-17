package org.rocs.asa.exception.domain;
/**
 * Exception thrown when an appointment is not found.
 */
public class AppointmentNotFoundException extends RuntimeException {
    /**
     * Constructs a new AppointmentNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
