package com.uzima.service;

import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.Job;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.JobAccount;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.ReceiptNumber;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.User;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * Records payments and reversals against jobs. The ledger is append-only; paid amount, balance and
 * status are always derived from it ({@link JobAccount}), never stored.
 *
 * <p>Each write locks the job row first, so two people recording payments on the same job at once
 * are checked one after the other and can't overpay it together.
 */
public final class PaymentService {

  static final String ENTITY = "Payment";

  private final Transactions tx;
  private final Session session;
  private final AuditLog audit;
  private final Clock clock;
  private final SequenceGenerator sequences;
  private final JobRepository jobs;
  private final PaymentRepository payments;

  public PaymentService(
      Transactions tx,
      Session session,
      AuditLog audit,
      Clock clock,
      SequenceGenerator sequences,
      JobRepository jobs,
      PaymentRepository payments) {
    this.tx = tx;
    this.session = session;
    this.audit = audit;
    this.clock = clock;
    this.sequences = sequences;
    this.jobs = jobs;
    this.payments = payments;
  }

  /** Records money received for a job. */
  public Payment.Received record(
      long jobId,
      Money amount,
      LocalDate date,
      PaymentMethod method,
      String reference,
      String notes) {
    User user = session.require(Permission.RECORD_PAYMENTS);
    return tx.inTransaction(
        () -> {
          jobs.lock(jobId);
          Job job = job(jobId);
          job.account(payments.findByJob(jobId)).checkCanReceive(amount, date, today());
          if (method == null) {
            throw new ValidationException("method", "Choose how the client paid");
          }
          Payment.Received saved =
              (Payment.Received)
                  payments.insert(
                      new Payment.Received(
                          0,
                          nextReceipt(),
                          jobId,
                          amount,
                          date,
                          method,
                          reference,
                          user.id(),
                          notes));
          audit.record(
              AuditAction.PAYMENT_RECORDED,
              ENTITY,
              saved.receiptNumber(),
              describe(saved, job)
                  + (saved.reference().isEmpty() ? "" : ", reference " + saved.reference()));
          return saved;
        });
  }

  /**
   * Reverses all or part of a received payment, e.g. to correct a mistake or to refund a cancelled
   * job. {@code amount} is the positive amount to give back.
   */
  public Payment.Reversal reverse(
      long paymentId,
      Money amount,
      LocalDate date,
      PaymentMethod refundMethod,
      String reference,
      String reason) {
    User user = session.require(Permission.REVERSE_PAYMENTS);
    Payment.Received original = received(paymentId);
    return tx.inTransaction(
        () -> {
          jobs.lock(original.jobId());
          Job job = job(original.jobId());
          JobAccount account = job.account(payments.findByJob(original.jobId()));
          account.checkCanReverse(original, amount, reason, date);
          if (date.isAfter(today())) {
            throw new ValidationException("date", "Reversal date cannot be in the future");
          }
          if (refundMethod == null) {
            throw new ValidationException("method", "Choose how the money is given back");
          }
          Payment.Reversal saved =
              (Payment.Reversal)
                  payments.insert(
                      new Payment.Reversal(
                          0,
                          nextReceipt(),
                          original.jobId(),
                          amount.negate(),
                          date,
                          refundMethod,
                          reference,
                          user.id(),
                          "",
                          original.id(),
                          reason));
          audit.record(
              AuditAction.PAYMENT_REVERSED,
              ENTITY,
              saved.receiptNumber(),
              describe(saved, job)
                  + ", reverses "
                  + original.receiptNumber()
                  + ", reason: "
                  + saved.reason());
          return saved;
        });
  }

  /** A job's ledger, oldest first. */
  public List<Payment> payments(long jobId) {
    session.requireUser();
    return payments.findByJob(jobId);
  }

  /** Derived paid amount, balance, status and refund due for a job. */
  public JobAccount account(long jobId) {
    session.requireUser();
    return job(jobId).account(payments.findByJob(jobId));
  }

  public Optional<Payment> find(long paymentId) {
    session.requireUser();
    return payments.findById(paymentId);
  }

  /** How much of a received payment can still be reversed. */
  public Money remainingToReverse(long paymentId) {
    session.requireUser();
    Payment.Received original = received(paymentId);
    return account(original.jobId()).remainingToReverse(original);
  }

  private Payment.Received received(long paymentId) {
    Payment payment =
        payments
            .findById(paymentId)
            .orElseThrow(() -> new DomainException("That payment no longer exists."));
    return switch (payment) {
      case Payment.Received r -> r;
      case Payment.Reversal r ->
          throw new DomainException(
              r.receiptNumber() + " is itself a reversal and cannot be reversed.");
    };
  }

  private Job job(long jobId) {
    return jobs.findById(jobId)
        .orElseThrow(() -> new DomainException("That job no longer exists."));
  }

  private ReceiptNumber nextReceipt() {
    return sequences.nextReceiptNumber(Year.from(today()));
  }

  private LocalDate today() {
    return LocalDate.ofInstant(clock.instant(), DisplayFormats.ZONE);
  }

  private static String describe(Payment p, Job job) {
    return p.amount().format()
        + " "
        + p.method().label()
        + " on "
        + DisplayFormats.DATE.format(p.date())
        + " for "
        + job.number();
  }
}
