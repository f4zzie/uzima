package com.uzima.service;

import static com.uzima.service.PaymentTestFixture.JOB_TOTAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.Job;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.JobAccount;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Role;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PaymentServiceTest {

  /** ServiceTestContext.START (07:00 UTC) is 10:00 on 1 October 2026 in Nairobi. */
  private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

  private final PaymentTestFixture f = new PaymentTestFixture();
  private final PaymentService service = f.service;

  @AfterEach
  void tearDown() {
    f.close();
  }

  private Payment.Received cash(Job job, String amount) {
    return service.record(job.id(), Money.of(amount), TODAY, PaymentMethod.CASH, "", "");
  }

  private static String field(Throwable e, String field) {
    return ((ValidationException) e).messageFor(field).orElse(null);
  }

  /** The job_accounts view must agree with the domain's derivation. */
  private void assertViewMatches(Job job) {
    JobAccount account = service.account(job.id());
    JobSummary view = f.jobs.summary(job.id()).orElseThrow();
    assertThat(view.amountPaid()).isEqualTo(account.netPaid());
    assertThat(view.balance()).isEqualTo(account.balance());
    assertThat(view.paymentStatus()).isEqualTo(account.status());
    assertThat(view.refundDue()).isEqualTo(account.refundDue());
  }

  @Nested
  class Recording {

    @Test
    void firstPaymentGetsFirstReceiptOfTheYearAndUpdatesTheAccount() {
      Job job = f.job();
      assertThat(service.account(job.id()).status()).isEqualTo(PaymentStatus.NOT_PAID);

      Payment.Received p = cash(job, "50000.00");

      assertThat(p.receiptNumber().value()).isEqualTo("RCP-2026-00001");
      assertThat(p.receivedBy()).isEqualTo(f.manager.id());
      JobAccount account = service.account(job.id());
      assertThat(account.status()).isEqualTo(PaymentStatus.PARTIALLY_PAID);
      assertThat(account.balance()).isEqualTo(Money.of("85720.00"));
      assertViewMatches(job);
    }

    @Test
    void exactPayoffMarksTheJobPaid() {
      Job job = f.job();
      cash(job, "100000.00");
      Payment.Received last = cash(job, "35720.00");

      assertThat(last.receiptNumber().value()).isEqualTo("RCP-2026-00002");
      assertThat(service.account(job.id()).status()).isEqualTo(PaymentStatus.PAID);
      assertThat(service.account(job.id()).balance()).isEqualTo(Money.ZERO);
      assertViewMatches(job);
    }

    @Test
    void overpaymentIsRejectedWithTheBalance() {
      Job job = f.job();
      cash(job, "100000.00");

      assertThatThrownBy(() -> cash(job, "40000.00"))
          .satisfies(
              e ->
                  assertThat(field(e, "amount"))
                      .contains("KSh 40,000.00")
                      .contains("KSh 35,720.00"));
      assertThat(service.payments(job.id())).hasSize(1);
    }

    @Test
    void amountMustBePositive() {
      Job job = f.job();
      assertThatThrownBy(() -> cash(job, "0.00"))
          .satisfies(e -> assertThat(field(e, "amount")).isNotNull());
      assertThatThrownBy(() -> cash(job, "-5.00"))
          .satisfies(e -> assertThat(field(e, "amount")).isNotNull());
    }

    @Test
    void futureDateIsRejected() {
      Job job = f.job();
      assertThatThrownBy(
              () ->
                  service.record(
                      job.id(), Money.of(10), TODAY.plusDays(1), PaymentMethod.CASH, "", ""))
          .satisfies(e -> assertThat(field(e, "date")).contains("future"));
    }

    @Test
    void mpesaReferenceMustLookLikeAReceiptAndIsUpperCased() {
      Job job = f.job();
      Payment.Received p =
          service.record(job.id(), Money.of(10), TODAY, PaymentMethod.MPESA, "qjk3h5l7pz", "");
      assertThat(p.reference()).isEqualTo("QJK3H5L7PZ");

      assertThatThrownBy(
              () -> service.record(job.id(), Money.of(10), TODAY, PaymentMethod.MPESA, "QJK3H", ""))
          .satisfies(e -> assertThat(field(e, "reference")).isNotNull());
    }

    @ParameterizedTest
    @EnumSource(
        value = PaymentMethod.class,
        names = {"BANK_TRANSFER", "CHEQUE"})
    void bankAndChequeNeedAReference(PaymentMethod method) {
      Job job = f.job();
      assertThatThrownBy(() -> service.record(job.id(), Money.of(10), TODAY, method, " ", ""))
          .satisfies(e -> assertThat(field(e, "reference")).isNotNull());
      assertThat(service.record(job.id(), Money.of(10), TODAY, method, "TRX-1", "").reference())
          .isEqualTo("TRX-1");
    }

    @Test
    void cashReferenceIsOptional() {
      assertThat(cash(f.job(), "10.00").reference()).isEmpty();
    }

    @Test
    void cancelledJobsTakeNoNewPayments() {
      Job job = f.job();
      f.cancel(job);
      assertThatThrownBy(() -> cash(job, "10.00"))
          .isInstanceOf(DomainException.class)
          .hasMessageContaining("cancelled");
    }

    @Test
    void anyStaffRoleMayRecordButSomeoneMustBeSignedIn() {
      Job job = f.job();
      f.ctx.signInAs(Role.CLERK);
      assertThat(cash(job, "10.00").receiptNumber()).isNotNull();

      f.ctx.signOut();
      assertThatThrownBy(() -> cash(job, "10.00")).isInstanceOf(NotSignedInException.class);
    }

    @Test
    void recordingIsAudited() {
      Job job = f.job();
      service.record(job.id(), Money.of(2500), TODAY, PaymentMethod.MPESA, "QJK3H5L7PZ", "");

      AuditEntry entry = f.ctx.auditEntries().getFirst();
      assertThat(entry.action()).isEqualTo(AuditAction.PAYMENT_RECORDED);
      assertThat(entry.entityId()).isEqualTo("RCP-2026-00001");
      assertThat(entry.username()).isEqualTo(f.manager.username().value());
      assertThat(entry.details())
          .contains("KSh 2,500.00", "M-Pesa", job.number().value(), "QJK3H5L7PZ");
    }

    @Test
    void failedAuditRollsBackThePaymentAndTheReceiptNumberIsReused() {
      Job job = f.job();
      AuditRepository failing =
          new AuditRepository() {
            @Override
            public long append(AuditEntry entry) {
              throw new IllegalStateException("audit store unavailable");
            }

            @Override
            public List<AuditEntry> search(AuditQuery query) {
              return List.of();
            }
          };
      PaymentService broken = f.withAudit(new AuditLog(failing, f.ctx.session, f.ctx.clock));

      assertThatThrownBy(
              () -> broken.record(job.id(), Money.of(10), TODAY, PaymentMethod.CASH, "", ""))
          .hasMessageContaining("audit store unavailable");
      assertThat(service.payments(job.id())).isEmpty();
      assertThat(cash(job, "10.00").receiptNumber().value()).isEqualTo("RCP-2026-00001");
    }
  }

  @Nested
  class Reversals {

    private Payment.Reversal refund(Payment original, String amount, LocalDate date) {
      return service.reverse(
          original.id(), Money.of(amount), date, PaymentMethod.CASH, "", "Keyed twice");
    }

    @Test
    void clerksCannotReverse() {
      Payment.Received p = cash(f.job(), "1000.00");
      f.ctx.signInAs(Role.CLERK);
      assertThatThrownBy(() -> refund(p, "1000.00", TODAY))
          .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void managerReversesWithOwnReceiptAndPaidBecomesPartlyPaid() {
      Job job = f.job();
      Payment.Received full = cash(job, "135720.00");
      assertThat(service.account(job.id()).status()).isEqualTo(PaymentStatus.PAID);

      Payment.Reversal r = refund(full, "20000.00", TODAY);

      assertThat(r.amount()).isEqualTo(Money.of("-20000.00"));
      assertThat(r.reversedPaymentId()).isEqualTo(full.id());
      assertThat(r.receiptNumber().value()).isEqualTo("RCP-2026-00002");
      assertThat(service.account(job.id()).status()).isEqualTo(PaymentStatus.PARTIALLY_PAID);
      assertThat(service.account(job.id()).balance()).isEqualTo(Money.of("20000.00"));
      assertViewMatches(job);
    }

    @Test
    void reasonIsRequired() {
      Payment.Received p = cash(f.job(), "1000.00");
      assertThatThrownBy(
              () -> service.reverse(p.id(), Money.of(10), TODAY, PaymentMethod.CASH, "", " "))
          .satisfies(e -> assertThat(field(e, "reason")).isNotNull());
    }

    @Test
    void partialReversalsStopAtTheOriginalAmount() {
      Payment.Received p = cash(f.job(), "1000.00");
      refund(p, "600.00", TODAY);
      assertThat(service.remainingToReverse(p.id())).isEqualTo(Money.of("400.00"));

      assertThatThrownBy(() -> refund(p, "400.01", TODAY))
          .satisfies(e -> assertThat(field(e, "amount")).contains("KSh 400.00"));
      refund(p, "400.00", TODAY);
      assertThat(service.remainingToReverse(p.id())).isEqualTo(Money.ZERO);
      assertThatThrownBy(() -> refund(p, "1.00", TODAY))
          .satisfies(e -> assertThat(field(e, "amount")).contains("fully reversed"));
    }

    @Test
    void aReversalCannotBeReversed() {
      Payment.Received p = cash(f.job(), "1000.00");
      Payment.Reversal r = refund(p, "100.00", TODAY);
      assertThatThrownBy(() -> refund(r, "50.00", TODAY))
          .isInstanceOf(DomainException.class)
          .hasMessageContaining("is itself a reversal");
    }

    @Test
    void reversalDateMustBeBetweenTheOriginalAndToday() {
      Job job = f.job();
      Payment.Received p =
          service.record(job.id(), Money.of(1000), TODAY, PaymentMethod.CASH, "", "");
      assertThatThrownBy(() -> refund(p, "10.00", TODAY.minusDays(1)))
          .satisfies(e -> assertThat(field(e, "date")).contains("before"));
      assertThatThrownBy(() -> refund(p, "10.00", TODAY.plusDays(1)))
          .satisfies(e -> assertThat(field(e, "date")).contains("future"));

      f.ctx.clock.advance(Duration.ofDays(3));
      assertThat(refund(p, "10.00", TODAY.plusDays(3)).date()).isEqualTo(TODAY.plusDays(3));
    }

    @ParameterizedTest
    @EnumSource(
        value = PaymentMethod.class,
        names = {"MPESA", "BANK_TRANSFER", "CHEQUE"})
    void nonCashRefundsNeedAReference(PaymentMethod method) {
      Payment.Received p = cash(f.job(), "1000.00");
      assertThatThrownBy(() -> service.reverse(p.id(), Money.of(10), TODAY, method, "", "Refund"))
          .satisfies(e -> assertThat(field(e, "reference")).isNotNull());
    }

    @Test
    void mpesaRefundReferenceIsAccepted() {
      Payment.Received p = cash(f.job(), "1000.00");
      Payment.Reversal r =
          service.reverse(p.id(), Money.of(10), TODAY, PaymentMethod.MPESA, "rfd3h5l7pz", "Refund");
      assertThat(r.reference()).isEqualTo("RFD3H5L7PZ");
    }

    @Test
    void reversalIsAudited() {
      Payment.Received p = cash(f.job(), "1000.00");
      refund(p, "250.00", TODAY);

      AuditEntry entry = f.ctx.auditEntries().getFirst();
      assertThat(entry.action()).isEqualTo(AuditAction.PAYMENT_REVERSED);
      assertThat(entry.entityId()).isEqualTo("RCP-2026-00002");
      assertThat(entry.details()).contains("-KSh 250.00", "RCP-2026-00001", "Keyed twice");
    }

    @Test
    void unknownPaymentIsReported() {
      assertThatThrownBy(() -> service.remainingToReverse(999)).isInstanceOf(DomainException.class);
      assertThat(service.find(999)).isEmpty();
    }
  }

  @Nested
  class CancelledJobs {

    @Test
    void cancelledJobOwesNothingAndShowsRefundDueUntilRefunded() {
      Job job = f.job();
      Payment.Received first = cash(job, "50000.00");
      Payment.Received second = cash(job, "20000.00");
      f.cancel(job);

      JobAccount account = service.account(job.id());
      assertThat(account.balance()).isEqualTo(Money.ZERO);
      assertThat(account.isOutstanding()).isFalse();
      assertThat(account.refundDue()).isEqualTo(Money.of("70000.00"));
      assertViewMatches(job);

      assertThatThrownBy(() -> cash(job, "1.00")).isInstanceOf(DomainException.class);

      service.reverse(
          first.id(), Money.of(50_000), TODAY, PaymentMethod.MPESA, "RFD3H5L7PZ", "Refund");
      assertThat(service.account(job.id()).refundDue()).isEqualTo(Money.of("20000.00"));
      assertViewMatches(job);

      service.reverse(second.id(), Money.of(20_000), TODAY, PaymentMethod.CASH, "", "Refund");
      account = service.account(job.id());
      assertThat(account.refundDue()).isEqualTo(Money.ZERO);
      assertThat(account.netPaid()).isEqualTo(Money.ZERO);
      assertViewMatches(job);
    }
  }

  @Test
  void lockingAJobNeedsATransactionAndAnExistingJob() {
    Job job = f.job();
    assertThatThrownBy(() -> f.jobs.lock(job.id())).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> f.ctx.tx.run(() -> f.jobs.lock(999_999)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void collectedVatOfAFullyPaidJobEqualsItsInvoicedVatToTheCent() {
    Job job = f.job();
    cash(job, "33333.33");
    cash(job, "33333.33");
    cash(job, "69053.34");
    assertThat(service.account(job.id()).status()).isEqualTo(PaymentStatus.PAID);

    List<Money> amounts = service.payments(job.id()).stream().map(Payment::amount).toList();
    List<Money> vat =
        f.engine()
            .collectedVat(
                amounts, job.quote().taxRate(), job.quote().grandTotal(), job.quote().tax());

    assertThat(Money.sum(vat.stream())).isEqualTo(job.quote().tax());
    assertThat(job.quote().tax()).isEqualTo(Money.of("18720.00"));
    assertThat(job.quote().grandTotal()).isEqualTo(JOB_TOTAL);
  }
}
