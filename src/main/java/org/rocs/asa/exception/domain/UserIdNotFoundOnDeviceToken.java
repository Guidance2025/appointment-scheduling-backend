package org.rocs.asa.exception.domain;

public class UserIdNotFoundOnDeviceToken extends RuntimeException {
    public UserIdNotFoundOnDeviceToken(String message) {
        super(message);
    }
}
