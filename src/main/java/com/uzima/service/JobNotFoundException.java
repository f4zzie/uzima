package com.uzima.service;

import com.uzima.domain.common.DomainException;

/** No job exists with the requested id. */
public final class JobNotFoundException extends DomainException {

  public JobNotFoundException(long id) {
    super("Job " + id + " was not found. It may have been removed or the list is out of date.");
  }
}
