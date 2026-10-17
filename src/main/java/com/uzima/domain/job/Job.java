package com.uzima.domain.job;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.JobAccount;
import com.uzima.domain.payment.Payment;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A quoted job and its lifecycle.
 *
 * <p>The price snapshot is three parts: the {@link Quote} (lines and totals that invoices and
 * reports read), the {@link QuoteSpec} (what was asked for, with each catalog item at the price it
 * was quoted at) and the {@link PriceBasis} (fees, rate tiers and tax rate). Editing a job that is
 * still {@link JobStatus#PENDING_SURVEY} re-prices against this snapshot, never the live catalog;
 * moving to current prices is a separate, explicit {@link #repriceTo} step.
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
    PriceBasis priceBasis,
    String notes,
    Instant createdAt,
    long createdBy) {

  public static final int MAX_NOTES = 2000;

  public Job {
    Objects.requireNonNull(number, "number");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(spec, "spec");
    Objects.requireNonNull(quote, "quote");
    Objects.requireNonNull(priceBasis, "priceBasis");
    requireSameTaxRate(quote, priceBasis);
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
      PriceBasis priceBasis,
      String notes,
      Instant createdAt,
      long createdBy) {
    return new Job(
        0,
        number,
        clientId,
        JobStatus.PENDING_SURVEY,
        spec,
        quote,
        priceBasis,
        notes,
        createdAt,
        createdBy);
  }

  /** The result of a status change: the updated job plus the history entry to record. */
  public record Transition(Job job, StatusChange change) {}

  /**
   * A proposed move of a pending job to current prices. Nothing changes until the caller saves
   * {@link #after()}; {@link #before()} is the job as it stands.
   */
  public record Repricing(Job before, Job after) {

    public Repricing {
      Objects.requireNonNull(before, "before");
      Objects.requireNonNull(after, "after");
    }

    public Quote beforeQuote() {
      return before.quote();
    }

    public Quote afterQuote() {
      return after.quote();
    }

    /** After minus before; negative when the job gets cheaper. */
    public Money grandTotalChange() {
      return after.quote().grandTotal().minus(before.quote().grandTotal());
    }

    /** False when current prices give exactly the same lines, totals and basis. */
    public boolean changesAnything() {
      return !before.quote().equals(after.quote())
          || !before.priceBasis().equals(after.priceBasis());
    }
  }

  /**
   * Replaces the spec and quote, keeping this job's {@link PriceBasis}. Only allowed while the job
   * is awaiting survey. The quote must have been priced with this job's basis.
   */
  public Job withQuote(QuoteSpec newSpec, Quote newQuote) {
    requireQuoteEditable();
    return new Job(
        id, number, clientId, status, newSpec, newQuote, priceBasis, notes, createdAt, createdBy);
  }

  /**
   * Proposes moving this pending job to current prices. {@code currentQuote} must be {@code
   * currentSpec} priced with {@code currentBasis}. This job is not modified.
   */
  public Repricing repriceTo(QuoteSpec currentSpec, PriceBasis currentBasis, Quote currentQuote) {
    requireQuoteEditable();
    Job after =
        new Job(
            id,
            number,
            clientId,
            status,
            currentSpec,
            currentQuote,
            currentBasis,
            notes,
            createdAt,
            createdBy);
    return new Repricing(this, after);
  }

  public Job withNotes(String newNotes) {
    return new Job(
        id, number, clientId, status, spec, quote, priceBasis, newNotes, createdAt, createdBy);
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
        new Job(
            id,
            number,
            clientId,
            change.to(),
            spec,
            quote,
            priceBasis,
            notes,
            createdAt,
            createdBy);
    return new Transition(moved, change);
  }

  private void requireQuoteEditable() {
    if (!status.isQuoteEditable()) {
      throw new QuoteLockedException(status);
    }
  }

  private static void requireSameTaxRate(Quote quote, PriceBasis basis) {
    if (!quote.taxRate().equals(basis.taxRate())) {
      throw new IllegalArgumentException(
          "Quote was priced at "
              + quote.taxRate()
              + " but the job's price basis is "
              + basis.taxRate());
    }
  }
}
