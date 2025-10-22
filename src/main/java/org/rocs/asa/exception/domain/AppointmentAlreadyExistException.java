package org.rocs.asa.exception.domain;

public class AppointmentAlreadyExistException extends RuntimeException {
    public AppointmentAlreadyExistException(String message) {
        super(message);
    }
}
