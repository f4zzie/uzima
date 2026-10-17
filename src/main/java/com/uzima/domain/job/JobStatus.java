package com.uzima.domain.job;

import java.util.Optional;

/**
 * Where a job is in its lifecycle. Jobs move forward one step at a time:
 *
 * <pre>
 * PENDING_SURVEY → APPROVED → IN_PROGRESS → COMPLETED
 * </pre>
 *
 * Any state except COMPLETED can be cancelled, and a CANCELLED job can be reopened to
 * PENDING_SURVEY. Changes are validated by {@link StatusChange}.
 */
public enum JobStatus {
  PENDING_SURVEY("Pending survey"),
  APPROVED("Approved"),
  IN_PROGRESS("In progress"),
  COMPLETED("Completed"),
  CANCELLED("Cancelled");

  private final String label;

  JobStatus(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  /** The single forward step from this state, if there is one. */
  public Optional<JobStatus> next() {
    return switch (this) {
      case PENDING_SURVEY -> Optional.of(APPROVED);
      case APPROVED -> Optional.of(IN_PROGRESS);
      case IN_PROGRESS -> Optional.of(COMPLETED);
      case COMPLETED, CANCELLED -> Optional.empty();
    };
  }

  /** True only for the single forward step; cancelling and reopening have their own checks. */
  public boolean canAdvanceTo(JobStatus target) {
    return next().filter(target::equals).isPresent();
  }

  public boolean canCancel() {
    return this != COMPLETED && this != CANCELLED;
  }

  public boolean canReopen() {
    return this == CANCELLED;
  }

  /** Quote lines are locked once the survey is approved. */
  public boolean isQuoteEditable() {
    return this == PENDING_SURVEY;
  }

  public boolean acceptsPayments() {
    return this != CANCELLED;
  }

  @Override
  public String toString() {
    return label;
  }
}
