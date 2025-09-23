package org.rocs.asa.exception.domain;

public class UserIdAlreadyExist extends RuntimeException {
    public UserIdAlreadyExist(String message) {
        super(message);
    }
}
