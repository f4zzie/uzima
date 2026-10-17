package com.uzima.domain.common;

/** Base class for business-rule violations. Messages are written for the end user. */
public class DomainException extends RuntimeException {

  public DomainException(String message) {
    super(message);
  }
}
