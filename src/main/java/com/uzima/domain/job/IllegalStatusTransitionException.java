package com.uzima.domain.job;

import com.uzima.domain.common.DomainException;

/** A requested status change that the job lifecycle does not allow. */
public final class IllegalStatusTransitionException extends DomainException {

  private final JobStatus from;
  private final JobStatus to;

  public IllegalStatusTransitionException(JobStatus from, JobStatus to) {
    super("A job that is " + from.label() + " cannot move to " + to.label());
    this.from = from;
    this.to = to;
  }

  public JobStatus from() {
    return from;
  }

  public JobStatus to() {
    return to;
  }
}
