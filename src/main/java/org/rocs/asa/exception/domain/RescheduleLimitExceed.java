package org.rocs.asa.exception.domain;

public class RescheduleLimitExceed extends RuntimeException {
    public RescheduleLimitExceed(String message) {
        super(message);
    }
}
