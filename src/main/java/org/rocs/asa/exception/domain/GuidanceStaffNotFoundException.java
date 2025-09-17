package org.rocs.asa.exception.domain;

/**
 * Exception thrown when a guidance staff member is not found.
 */
public class GuidanceStaffNotFoundException extends RuntimeException {
    /**
     * Constructs a new GuidanceStaffNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public GuidanceStaffNotFoundException(String message) {
        super(message);
    }
}
