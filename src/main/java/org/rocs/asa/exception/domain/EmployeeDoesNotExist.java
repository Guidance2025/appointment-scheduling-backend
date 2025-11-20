package org.rocs.asa.exception.domain;

public class EmployeeDoesNotExist extends RuntimeException {
    public EmployeeDoesNotExist(String message) {
        super(message);
    }
}
