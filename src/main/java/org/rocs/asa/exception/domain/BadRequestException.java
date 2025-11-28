package org.rocs.asa.exception.domain;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) { super(message); }
}