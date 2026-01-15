package org.rocs.asa.exception.domain;

public class AppointmentUpdateFailedException extends RuntimeException {
    public AppointmentUpdateFailedException(String message) {
        super(message);
    }
}
