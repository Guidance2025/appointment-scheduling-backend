package org.rocs.asa.exception.domain;

public class LockedException extends RuntimeException {
  public LockedException(String message) {
    super(message);
  }
}
