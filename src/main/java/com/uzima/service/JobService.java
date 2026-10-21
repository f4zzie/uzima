package com.uzima.service;

import com.uzima.domain.client.Client;
import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.User;
import com.uzima.pricing.PricingEngine;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Quotes and jobs: pricing a quote, saving it as a job, editing it while it awaits survey,
 * re-pricing it on request, and moving it through its lifecycle. Every price comes from {@link
 * PricingEngine}; every write is one transaction with its audit entry.
 */
public final class JobService {

  static final String ENTITY = "Job";

  private final Transactions tx;
  private final Session session;
  private final AuditLog audit;
  private final Clock clock;
  private final SequenceGenerator sequences;
  private final JobRepository jobs;
  private final ClientRepository clients;
  private final CatalogService catalog;
  private final SettingsService settings;
  private final PricingEngine engine;

  public JobService(
      Transactions tx,
      Session session,
      AuditLog audit,
      Clock clock,
      SequenceGenerator sequences,
      JobRepository jobs,
      ClientRepository clients,
      CatalogService catalog,
      SettingsService settings,
      PricingEngine engine) {
    this.tx = tx;
    this.session = session;
    this.audit = audit;
    this.clock = clock;
    this.sequences = sequences;
    this.jobs = jobs;
    this.clients = clients;
    this.catalog = catalog;
    this.settings = settings;
    this.engine = engine;
  }

  /** Prices {@code spec} for {@code clientId} at today's prices without saving anything. */
  public Quote preview(long clientId, QuoteSpec spec) {
    session.require(Permission.CREATE_JOBS);
    return engine.quote(spec, currentBasis(client(clientId)));
  }

  /**
   * Prices an edit to a pending job the way {@link #editQuote} would save it: items already on the
   * job at their snapshotted prices, with the job's own fees, tiers and tax rate. Nothing is saved.
   *
   * @throws com.uzima.domain.job.QuoteLockedException if the job has left PENDING_SURVEY
   */
  public Quote previewEdit(long jobId, QuoteSpec edited) {
    session.require(Permission.CREATE_JOBS);
    return engine.requote(load(jobId), edited).quote();
  }

  /** Saves a new job awaiting survey, with its prices snapshotted. */
  public Job create(long clientId, QuoteSpec spec, String notes) {
    User user = session.require(Permission.CREATE_JOBS);
    return tx.inTransaction(
        () -> {
          Client client = client(clientId);
          if (!client.active()) {
            throw new DomainException(
                client.name() + " is deactivated. Reactivate the client before quoting.");
          }
          Instant now = now();
          PriceBasis basis = currentBasis(client);
          Quote quote = engine.quote(spec, basis);
          JobNumber number = sequences.nextJobNumber(Year.from(now.atZone(DisplayFormats.ZONE)));
          Job saved =
              jobs.insert(Job.create(number, clientId, spec, quote, basis, notes, now, user.id()));
          audit.record(
              AuditAction.JOB_CREATED,
              ENTITY,
              number,
              "client: "
                  + client.code()
                  + "; grand total: "
                  + quote.grandTotal()
                  + "; tax rate: "
                  + quote.taxRate());
          return saved;
        });
  }

  /**
   * Changes what a pending job includes. Items already on the job keep their snapshotted prices,
   * and the job's fees, tiers and tax rate are unchanged; see {@link #previewReprice} for moving to
   * current prices.
   */
  public Job editQuote(long jobId, QuoteSpec edited, String notes) {
    session.require(Permission.CREATE_JOBS);
    return tx.inTransaction(
        () -> {
          Job before = load(jobId);
          Job after = engine.requote(before, edited).withNotes(notes);
          Changes changes =
              new Changes()
                  .field("grand total", before.quote().grandTotal(), after.quote().grandTotal())
                  .field("notes", before.notes(), after.notes());
          if (after.equals(before)) {
            return before;
          }
          jobs.replaceQuote(after, now());
          audit.record(
              AuditAction.JOB_QUOTE_EDITED,
              ENTITY,
              before.number(),
              changes.isEmpty() ? "services changed; totals unchanged" : changes.toString());
          return after;
        });
  }

  /**
   * What the job would cost at today's catalog prices, fees (for the client's current category) and
   * tax rate. Nothing is saved; call {@link #applyReprice} to accept it.
   */
  public Job.Repricing previewReprice(long jobId) {
    session.require(Permission.CREATE_JOBS);
    return reprice(load(jobId));
  }

  /**
   * Re-prices a pending job at current prices. {@code expectedGrandTotal} is the total the user saw
   * in {@link #previewReprice}; if prices moved since then, nothing is saved and the user is asked
   * to review again.
   */
  public Job applyReprice(long jobId, Money expectedGrandTotal) {
    session.require(Permission.CREATE_JOBS);
    return tx.inTransaction(
        () -> {
          Job.Repricing repricing = reprice(load(jobId));
          Quote before = repricing.beforeQuote();
          Quote after = repricing.afterQuote();
          if (!after.grandTotal().equals(expectedGrandTotal)) {
            throw new DomainException(
                "Prices changed since you reviewed this re-price (new total "
                    + after.grandTotal()
                    + "). Please review it again.");
          }
          if (!repricing.changesAnything()) {
            return repricing.before();
          }
          jobs.replaceQuote(repricing.after(), now());
          audit.record(
              AuditAction.JOB_REPRICED,
              ENTITY,
              repricing.before().number(),
              new Changes()
                  .field("grand total", before.grandTotal(), after.grandTotal())
                  .field("tax rate", before.taxRate(), after.taxRate())
                  .field(
                      "category",
                      repricing.before().priceBasis().fees().category().label(),
                      repricing.after().priceBasis().fees().category().label())
                  .toString());
          return repricing.after();
        });
  }

  /** Moves a job one step forward. Cancelling goes through {@link #cancel}. */
  public Job advance(long jobId, JobStatus target) {
    User user = session.require(Permission.ADVANCE_JOB_STATUS);
    if (target == JobStatus.CANCELLED) {
      throw new DomainException("Use Cancel job to cancel a job; a reason is required.");
    }
    return tx.inTransaction(
        () -> {
          Job job = load(jobId);
          Job.Transition t = job.advanceTo(target, now(), user.id());
          return apply(t, AuditAction.JOB_STATUS_CHANGED);
        });
  }

  /** Cancels a job that isn't completed. A reason is required. */
  public Job cancel(long jobId, String reason) {
    User user = session.require(Permission.CANCEL_JOB);
    return tx.inTransaction(
        () -> apply(load(jobId).cancel(reason, now(), user.id()), AuditAction.JOB_CANCELLED));
  }

  /** Returns a cancelled job to Pending survey. Administrators only; a reason is required. */
  public Job reopen(long jobId, String reason) {
    User user = session.require(Permission.REOPEN_JOB);
    return tx.inTransaction(
        () -> apply(load(jobId).reopen(reason, now(), user.id()), AuditAction.JOB_REOPENED));
  }

  public Job updateNotes(long jobId, String notes) {
    session.require(Permission.CREATE_JOBS);
    return tx.inTransaction(
        () -> {
          Job before = load(jobId);
          Job after = before.withNotes(notes);
          if (after.notes().equals(before.notes())) {
            return before;
          }
          jobs.updateNotes(jobId, after.notes(), now());
          audit.record(
              AuditAction.JOB_QUOTE_EDITED,
              ENTITY,
              before.number(),
              new Changes().field("notes", before.notes(), after.notes()).toString());
          return after;
        });
  }

  public Optional<Job> find(long jobId) {
    session.requireUser();
    return jobs.findById(jobId);
  }

  public Optional<JobSummary> summary(long jobId) {
    session.requireUser();
    return jobs.summary(jobId);
  }

  public List<JobSummary> search(JobSearch search) {
    session.requireUser();
    return jobs.search(search);
  }

  public List<StatusHistoryEntry> history(long jobId) {
    session.requireUser();
    return jobs.history(jobId);
  }

  private Job apply(Job.Transition t, AuditAction action) {
    jobs.recordStatusChange(t.job().id(), t.change());
    String details =
        new Changes().field("status", t.change().from().label(), t.change().to().label())
            + (t.change().reason().isEmpty() ? "" : "; reason: " + t.change().reason());
    audit.record(action, ENTITY, t.job().number(), details);
    return t.job();
  }

  private Job.Repricing reprice(Job job) {
    return engine.reprice(job, catalog.currentPrices(), currentBasis(client(job.clientId())));
  }

  private PriceBasis currentBasis(Client client) {
    return catalog.currentBasis(client.category(), settings.currentTaxRate());
  }

  private Client client(long clientId) {
    return clients
        .findById(clientId)
        .orElseThrow(() -> new DomainException("Client " + clientId + " was not found."));
  }

  private Job load(long jobId) {
    return jobs.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
  }

  /** Stored instants have millisecond precision; truncate so saved and reloaded jobs are equal. */
  private Instant now() {
    return clock.instant().truncatedTo(ChronoUnit.MILLIS);
  }
}
