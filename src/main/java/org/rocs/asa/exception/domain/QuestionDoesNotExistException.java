package org.rocs.asa.exception.domain;

public class QuestionDoesNotExistException extends RuntimeException {
    public QuestionDoesNotExistException(String message) {
        super(message);
    }
}
