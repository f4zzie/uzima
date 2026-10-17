package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.job.Job;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.persistence.TestDatabases.Kind;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JdbcPaymentRepositoryTest {

  private static final LocalDate DAY = LocalDate.of(2026, 10, 4);

  private static Job job(JobFixtures f) {
    long client = f.client("A", ClientCategory.DOMESTIC);
    return f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void receivedPaymentsAndReversalsRoundTripExactly(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      Job job = job(f);
      Payment.Received bank =
          (Payment.Received)
              f.tx.inTransaction(
                  () ->
                      f.payments.insert(
                          new Payment.Received(
                              0,
                              f.receipt(),
                              job.id(),
                              Money.of("15000.50"),
                              DAY,
                              PaymentMethod.BANK_TRANSFER,
                              "TRX-2026-881",
                              f.userId,
                              "First instalment")));
      Payment.Reversal refund =
          (Payment.Reversal)
              f.tx.inTransaction(
                  () ->
                      f.payments.insert(
                          new Payment.Reversal(
                              0,
                              f.receipt(),
                              job.id(),
                              Money.of("-500.50"),
                              DAY.plusDays(1),
                              PaymentMethod.MPESA,
                              "RFD3H5L7PZ",
                              f.userId,
                              "",
                              bank.id(),
                              "Overcharged survey")));

      assertThat(bank.id()).isPositive();
      assertThat(f.payments.findById(bank.id())).contains(bank);
      assertThat(f.payments.findById(refund.id())).contains(refund);
      assertThat(f.payments.findByJob(job.id())).containsExactly(bank, refund);
      assertThat(f.payments.findById(999)).isEmpty();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void ledgerIsOrderedByPaymentDateThenEntryOrder(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      Job job = job(f);
      Payment later = f.pay(job, 1_000, DAY.plusDays(2));
      Payment earlier = f.pay(job, 2_000, DAY);
      Payment sameDay = f.pay(job, 3_000, DAY);

      assertThat(f.payments.findByJob(job.id())).containsExactly(earlier, sameDay, later);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void paymentsMustBeSavedInsideATransaction(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      Job job = job(f);
      Payment p =
          new Payment.Received(
              0, f.receipt(), job.id(), Money.of(10), DAY, PaymentMethod.CASH, "", f.userId, "");

      assertThatThrownBy(() -> f.payments.insert(p)).isInstanceOf(IllegalStateException.class);
      assertThat(f.count("payments")).isZero();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void databaseRejectsAPositiveReversal(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      Job job = job(f);
      Payment original = f.pay(job, 1_000, DAY);

      assertThatThrownBy(
              () ->
                  f.jdbc.insert(
                      "INSERT INTO payments (receipt_number, job_id, amount, payment_date, method,"
                          + " received_by, reversal_of, reason, created_at)"
                          + " VALUES ('RCP-2026-99999', ?, 100.00, ?, 'CASH', ?, ?, 'x', ?)",
                      job.id(),
                      DAY,
                      f.userId,
                      original.id(),
                      Instant.EPOCH))
          .isInstanceOf(PersistenceException.class);
    }
  }
}
