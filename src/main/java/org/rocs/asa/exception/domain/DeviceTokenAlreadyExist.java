package org.rocs.asa.exception.domain;

public class DeviceTokenAlreadyExist extends RuntimeException {
    public DeviceTokenAlreadyExist(String message) {
        super(message);
    }
}
