package com.uzima.service;

import com.uzima.domain.common.DomainException;

/** A protected operation was called with nobody signed in. */
public final class NotSignedInException extends DomainException {

  public NotSignedInException() {
    super("Please sign in to continue.");
  }
}
