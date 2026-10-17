package com.uzima.domain.job;

import com.uzima.domain.common.ValidationException;
import java.time.Instant;
import java.util.Objects;

/**
 * One validated entry in a job's status history. Create instances through {@link #advance}, {@link
 * #cancel} or {@link #reopen}, which enforce the lifecycle rules in {@link JobStatus}.
 */
public record StatusChange(
    JobStatus from, JobStatus to, String reason, Instant at, long changedBy) {

  public static final int MAX_REASON = 500;

  public StatusChange {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    Objects.requireNonNull(at, "at");
    reason = reason == null ? "" : reason.strip();
    if (reason.length() > MAX_REASON) {
      throw new ValidationException("reason", "Reason is too long");
    }
  }

  /** Moves one step forward. A reason is optional. */
  public static StatusChange advance(JobStatus from, JobStatus to, Instant at, long by) {
    return advance(from, to, "", at, by);
  }

  public static StatusChange advance(
      JobStatus from, JobStatus to, String reason, Instant at, long by) {
    if (!from.canAdvanceTo(to)) {
      throw new IllegalStatusTransitionException(from, to);
    }
    return new StatusChange(from, to, reason, at, by);
  }

  /** Cancels a job that is not yet completed. A reason is required. */
  public static StatusChange cancel(JobStatus from, String reason, Instant at, long by) {
    if (!from.canCancel()) {
      throw new IllegalStatusTransitionException(from, JobStatus.CANCELLED);
    }
    requireReason(reason, "Give a reason for cancelling the job");
    return new StatusChange(from, JobStatus.CANCELLED, reason, at, by);
  }

  /** Reopens a cancelled job to {@link JobStatus#PENDING_SURVEY}. A reason is required. */
  public static StatusChange reopen(JobStatus from, String reason, Instant at, long by) {
    if (!from.canReopen()) {
      throw new IllegalStatusTransitionException(from, JobStatus.PENDING_SURVEY);
    }
    requireReason(reason, "Give a reason for reopening the job");
    return new StatusChange(from, JobStatus.PENDING_SURVEY, reason, at, by);
  }

  private static void requireReason(String reason, String message) {
    if (reason == null || reason.isBlank()) {
      throw new ValidationException("reason", message);
    }
  }
}
