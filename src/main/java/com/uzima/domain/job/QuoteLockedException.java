package com.uzima.domain.job;

import com.uzima.domain.common.DomainException;

/** Thrown when someone tries to change the priced lines of a job that has left PENDING_SURVEY. */
public final class QuoteLockedException extends DomainException {

  public QuoteLockedException(JobStatus status) {
    super(
        "This job is "
            + status.label()
            + ". Its quote lines are locked once the job leaves "
            + JobStatus.PENDING_SURVEY.label()
            + ".");
  }
}
