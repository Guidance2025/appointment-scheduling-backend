package org.rocs.asa.exception.domain;

public class QuestionNotFoundException extends RuntimeException {
  public QuestionNotFoundException(String message) {
    super(message);
  }

}