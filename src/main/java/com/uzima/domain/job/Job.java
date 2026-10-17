package com.uzima.domain.job;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.payment.JobAccount;
import com.uzima.domain.payment.Payment;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A quoted job and its lifecycle. The {@link Quote} is the price snapshot: once saved, invoices and
 * reports read it and never the live catalog. The {@link QuoteSpec} records what was asked for, so
 * a job still in {@link JobStatus#PENDING_SURVEY} can be re-opened in the editor and re-priced.
 *
 * <p>Payment state is never stored on the job; it is derived with {@link #account(List)}.
 */
public record Job(
    long id,
    JobNumber number,
    long clientId,
    JobStatus status,
    QuoteSpec spec,
    Quote quote,
    String notes,
    Instant createdAt,
    long createdBy) {

  public static final int MAX_NOTES = 2000;

  public Job {
    Objects.requireNonNull(number, "number");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(spec, "spec");
    Objects.requireNonNull(quote, "quote");
    Objects.requireNonNull(createdAt, "createdAt");
    notes = notes == null ? "" : notes.strip();
    if (notes.length() > MAX_NOTES) {
      throw new ValidationException("notes", "Notes are too long");
    }
  }

  /** A new, unsaved job awaiting survey. */
  public static Job create(
      JobNumber number,
      long clientId,
      QuoteSpec spec,
      Quote quote,
      String notes,
      Instant createdAt,
      long createdBy) {
    return new Job(
        0, number, clientId, JobStatus.PENDING_SURVEY, spec, quote, notes, createdAt, createdBy);
  }

  /** The result of a status change: the updated job plus the history entry to record. */
  public record Transition(Job job, StatusChange change) {}

  /** Replaces the quote. Only allowed while the job is awaiting survey. */
  public Job withQuote(QuoteSpec newSpec, Quote newQuote) {
    if (!status.isQuoteEditable()) {
      throw new QuoteLockedException(status);
    }
    return new Job(id, number, clientId, status, newSpec, newQuote, notes, createdAt, createdBy);
  }

  public Job withNotes(String newNotes) {
    return new Job(id, number, clientId, status, spec, quote, newNotes, createdAt, createdBy);
  }

  /** Moves one step forward (PENDING_SURVEY → APPROVED → IN_PROGRESS → COMPLETED). */
  public Transition advanceTo(JobStatus target, Instant at, long by) {
    return apply(StatusChange.advance(status, target, at, by));
  }

  /** Cancels from any non-terminal state. A reason is required. */
  public Transition cancel(String reason, Instant at, long by) {
    return apply(StatusChange.cancel(status, reason, at, by));
  }

  /** Returns a cancelled job to PENDING_SURVEY. A reason is required. */
  public Transition reopen(String reason, Instant at, long by) {
    return apply(StatusChange.reopen(status, reason, at, by));
  }

  /** Derived payment state for this job given its payment ledger. */
  public JobAccount account(List<Payment> payments) {
    return new JobAccount(quote.grandTotal(), status == JobStatus.CANCELLED, payments);
  }

  private Transition apply(StatusChange change) {
    Job moved =
        new Job(id, number, clientId, change.to(), spec, quote, notes, createdAt, createdBy);
    return new Transition(moved, change);
  }
}
