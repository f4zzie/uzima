package com.uzima.domain.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.domain.payment.ReceiptNumber;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JobTest {

  private static final Instant T0 = Instant.parse("2026-10-01T07:00:00Z");
  private static final long CLERK = 3;
  private static final long MANAGER = 2;

  /** A quote of one manual plumbing line, priced by hand so this test needs no PricingEngine. */
  private static Quote quote(long net) {
    Money amount = Money.of(net);
    QuoteLine line =
        new QuoteLine(
            LineCategory.PLUMBING, "Plumbing", BigDecimal.ONE, QuantityUnit.EACH, amount, amount);
    Money tax = amount.times(new BigDecimal("0.16"));
    return new Quote(List.of(line), amount, TaxRate.KENYA_STANDARD, tax, amount.plus(tax));
  }

  private static QuoteSpec spec(long net) {
    return new QuoteSpec(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.of(new Plumbing.Manual(Money.of(net), "Plumbing")));
  }

  private static Job newJob() {
    return Job.create(
        new JobNumber(Year.of(2026), 1), 7, spec(100_000), quote(100_000), "", T0, CLERK);
  }

  private static Payment.Received paid(long id, long shillings) {
    return new Payment.Received(
        id,
        new ReceiptNumber(Year.of(2026), (int) id),
        1,
        Money.of(shillings),
        LocalDate.of(2026, 10, 2),
        PaymentMethod.CASH,
        "",
        CLERK,
        "");
  }

  @Test
  void newJobAwaitsSurvey() {
    Job job = newJob();
    assertThat(job.status()).isEqualTo(JobStatus.PENDING_SURVEY);
    assertThat(job.id()).isZero();
    assertThat(job.quote().grandTotal()).isEqualTo(Money.of(116_000));
  }

  @Test
  void quoteCanBeReplacedWhileAwaitingSurvey() {
    Job edited = newJob().withQuote(spec(200_000), quote(200_000));
    assertThat(edited.quote().grandTotal()).isEqualTo(Money.of(232_000));
  }

  @Test
  void quoteIsLockedOnceApproved() {
    Job approved = newJob().advanceTo(JobStatus.APPROVED, T0, CLERK).job();
    assertThatThrownBy(() -> approved.withQuote(spec(1), quote(1)))
        .isInstanceOf(QuoteLockedException.class)
        .hasMessageContaining("locked");
  }

  @Test
  void transitionsReturnTheHistoryEntry() {
    Job.Transition t = newJob().advanceTo(JobStatus.APPROVED, T0, CLERK);
    assertThat(t.job().status()).isEqualTo(JobStatus.APPROVED);
    assertThat(t.change().from()).isEqualTo(JobStatus.PENDING_SURVEY);
    assertThat(t.change().to()).isEqualTo(JobStatus.APPROVED);
    assertThat(t.change().changedBy()).isEqualTo(CLERK);
  }

  @Test
  void fullLifecycleToCompleted() {
    Job job = newJob();
    for (JobStatus next : List.of(JobStatus.APPROVED, JobStatus.IN_PROGRESS, JobStatus.COMPLETED)) {
      job = job.advanceTo(next, T0, CLERK).job();
    }
    assertThat(job.status()).isEqualTo(JobStatus.COMPLETED);
    Job completed = job;
    assertThatThrownBy(() -> completed.cancel("Too late", T0, MANAGER))
        .isInstanceOf(DomainException.class);
  }

  @Test
  void skippingAStateIsRejected() {
    assertThatThrownBy(() -> newJob().advanceTo(JobStatus.IN_PROGRESS, T0, CLERK))
        .isInstanceOf(IllegalStatusTransitionException.class);
  }

  @Test
  void cancelledJobWithPaymentsOwesNothingAndShowsRefundDue() {
    Job cancelled = newJob().cancel("Client withdrew", T0, MANAGER).job();
    var account = cancelled.account(List.of(paid(1, 50_000)));

    assertThat(account.balance()).isEqualTo(Money.ZERO);
    assertThat(account.isOutstanding()).isFalse();
    assertThat(account.refundDue()).isEqualTo(Money.of(50_000));
  }

  @Test
  void reopenReturnsCancelledJobToPendingSurveyAndUnlocksQuote() {
    Job reopened =
        newJob()
            .cancel("Client withdrew", T0, MANAGER)
            .job()
            .reopen("Client came back", T0, 1)
            .job();
    assertThat(reopened.status()).isEqualTo(JobStatus.PENDING_SURVEY);
    assertThat(reopened.withQuote(spec(5), quote(5)).quote().subtotal()).isEqualTo(Money.of(5));
  }

  @Test
  void accountIsDerivedFromPayments() {
    var account = newJob().account(List.of(paid(1, 16_000)));
    assertThat(account.netPaid()).isEqualTo(Money.of(16_000));
    assertThat(account.balance()).isEqualTo(Money.of(100_000));
    assertThat(account.status()).isEqualTo(PaymentStatus.PARTIALLY_PAID);
  }

  @Test
  void notesAreTrimmedAndBounded() {
    assertThat(newJob().withNotes("  borehole near gate  ").notes())
        .isEqualTo("borehole near gate");
    assertThatThrownBy(() -> newJob().withNotes("x".repeat(Job.MAX_NOTES + 1)))
        .isInstanceOf(ValidationException.class);
  }
}
