package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.JobAccount;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.persistence.TestDatabases.Kind;
import com.uzima.service.JobSearch;
import com.uzima.service.JobSummary;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JobSearchTest {

  private static final LocalDate PAID_ON = LocalDate.of(2026, 10, 4);
  private static final Instant LATER = Instant.parse("2026-10-04T10:00:00Z");

  /** Tank-only domestic job: 15,000 + 7,000 + 10,000 = 32,000 + 16% VAT = 37,120.00. */
  private static Job domesticJob(JobFixtures f, long client, Instant createdAt) {
    return f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), createdAt));
  }

  private static Job cancel(JobFixtures f, Job job) {
    Job.Transition t = job.cancel("Client withdrew", LATER, f.userId);
    f.tx.run(() -> f.jobs.recordStatusChange(job.id(), t.change()));
    return t.job();
  }

  /** The view's derived columns must agree with the domain's JobAccount for the same ledger. */
  private static void assertMatchesDomain(JobFixtures f, Job job) {
    JobSummary s = f.jobs.summary(job.id()).orElseThrow();
    JobAccount account = job.account(f.payments.findByJob(job.id()));
    assertThat(s.amountPaid()).isEqualTo(account.netPaid());
    assertThat(s.balance()).isEqualTo(account.balance());
    assertThat(s.paymentStatus()).isEqualTo(account.status());
    assertThat(s.refundDue()).isEqualTo(account.refundDue());
    assertThat(s.grandTotal()).isEqualTo(job.quote().grandTotal());
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void derivedPaymentStateMatchesTheDomainInEveryCase(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("Wanjiru", ClientCategory.DOMESTIC);

      Job unpaid = domesticJob(f, client, JobFixtures.CREATED);
      assertMatchesDomain(f, unpaid);
      assertThat(f.jobs.summary(unpaid.id()).orElseThrow().paymentStatus())
          .isEqualTo(PaymentStatus.NOT_PAID);

      Job partial = domesticJob(f, client, JobFixtures.CREATED);
      f.pay(partial, 10_000, PAID_ON);
      assertMatchesDomain(f, partial);
      assertThat(f.jobs.summary(partial.id()).orElseThrow().balance())
          .isEqualTo(Money.of("27120.00"));

      Job paid = domesticJob(f, client, JobFixtures.CREATED);
      f.pay(paid, 20_000, PAID_ON);
      f.tx.run(
          () ->
              f.payments.insert(
                  new Payment.Received(
                      0,
                      f.receipt(),
                      paid.id(),
                      Money.of("17120.00"),
                      PAID_ON,
                      PaymentMethod.CASH,
                      "",
                      f.userId,
                      "")));
      assertMatchesDomain(f, paid);
      assertThat(f.jobs.summary(paid.id()).orElseThrow().paymentStatus())
          .isEqualTo(PaymentStatus.PAID);

      Job reversed = domesticJob(f, client, JobFixtures.CREATED);
      Payment.Received keyedTwice = f.pay(reversed, 37_000, PAID_ON);
      f.reverse(keyedTwice, 20_000, PAID_ON);
      assertMatchesDomain(f, reversed);
      assertThat(f.jobs.summary(reversed.id()).orElseThrow().amountPaid())
          .isEqualTo(Money.of(17_000));

      Job cancelledWithMoney = domesticJob(f, client, JobFixtures.CREATED);
      f.pay(cancelledWithMoney, 12_000, PAID_ON);
      cancelledWithMoney = cancel(f, cancelledWithMoney);
      assertMatchesDomain(f, cancelledWithMoney);
      JobSummary refundDue = f.jobs.summary(cancelledWithMoney.id()).orElseThrow();
      assertThat(refundDue.balance()).isEqualTo(Money.ZERO);
      assertThat(refundDue.refundDue()).isEqualTo(Money.of(12_000));

      Job refunded = domesticJob(f, client, JobFixtures.CREATED);
      Payment.Received deposit = f.pay(refunded, 12_000, PAID_ON);
      refunded = cancel(f, refunded);
      f.reverse(deposit, 12_000, PAID_ON.plusDays(1));
      assertMatchesDomain(f, refunded);
      assertThat(f.jobs.summary(refunded.id()).orElseThrow().refundDue()).isEqualTo(Money.ZERO);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void filtersByStatusPaymentStatusClientAndCategory(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long domestic = f.client("Wanjiru Homes", ClientCategory.DOMESTIC);
      long industrial = f.client("Thika Mills", ClientCategory.INDUSTRIAL);
      Job a = domesticJob(f, domestic, JobFixtures.CREATED);
      Job b = domesticJob(f, domestic, JobFixtures.CREATED.plusSeconds(60));
      Job c =
          f.save(
              f.newJob(
                  industrial, ClientCategory.INDUSTRIAL, f.fullService(), JobFixtures.CREATED));
      f.pay(b, 1_000, PAID_ON);
      Job.Transition approved = c.advanceTo(JobStatus.APPROVED, LATER, f.userId);
      f.tx.run(() -> f.jobs.recordStatusChange(c.id(), approved.change()));

      assertThat(ids(f, JobSearch.all())).containsExactlyInAnyOrder(a.id(), b.id(), c.id());
      assertThat(ids(f, JobSearch.all().withStatus(JobStatus.APPROVED))).containsExactly(c.id());
      assertThat(ids(f, JobSearch.all().withPaymentStatus(PaymentStatus.PARTIALLY_PAID)))
          .containsExactly(b.id());
      assertThat(ids(f, JobSearch.all().withPaymentStatus(PaymentStatus.NOT_PAID)))
          .containsExactlyInAnyOrder(a.id(), c.id());
      assertThat(ids(f, JobSearch.all().withClient(domestic))).containsExactly(b.id(), a.id());
      assertThat(ids(f, JobSearch.all().withClientCategory(ClientCategory.INDUSTRIAL)))
          .containsExactly(c.id());
      assertThat(ids(f, JobSearch.all().withLimit(1))).hasSize(1);

      JobSummary summary = f.jobs.summary(c.id()).orElseThrow();
      assertThat(summary.clientName()).isEqualTo("Thika Mills");
      assertThat(summary.clientCode().value()).isEqualTo("UZM-IND-0002");
      assertThat(summary.number()).isEqualTo(c.number());
      assertThat(summary.status()).isEqualTo(JobStatus.APPROVED);
      assertThat(summary.createdAt()).isEqualTo(JobFixtures.CREATED);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void freeTextMatchesJobNumberClientNameAndCodeAndEscapesWildcards(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long wanjiru = f.client("Wanjiru Homes", ClientCategory.DOMESTIC);
      long other = f.client("100% Pure Water", ClientCategory.DOMESTIC);
      Job a = domesticJob(f, wanjiru, JobFixtures.CREATED);
      Job b = domesticJob(f, other, JobFixtures.CREATED);

      assertThat(ids(f, JobSearch.all().withText("wanJIRU"))).containsExactly(a.id());
      assertThat(ids(f, JobSearch.all().withText(b.number().value()))).containsExactly(b.id());
      assertThat(ids(f, JobSearch.all().withText("uzm-dom-0001"))).containsExactly(a.id());
      assertThat(ids(f, JobSearch.all().withText("100%"))).containsExactly(b.id());
      assertThat(ids(f, JobSearch.all().withText("%"))).containsExactly(b.id());
      assertThat(ids(f, JobSearch.all().withText("_"))).isEmpty();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void dateRangeUsesNairobiCalendarDays(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      // 23:59:59.999 on 1 October in Nairobi (UTC+3) ...
      Job lateFirst = domesticJob(f, client, Instant.parse("2026-10-01T20:59:59.999Z"));
      // ... and 00:00 on 2 October in Nairobi, although still 1 October in UTC.
      Job earlySecond = domesticJob(f, client, Instant.parse("2026-10-01T21:00:00Z"));

      LocalDate first = LocalDate.of(2026, 10, 1);
      LocalDate second = LocalDate.of(2026, 10, 2);
      assertThat(ids(f, JobSearch.all().withCreatedBetween(first, first)))
          .containsExactly(lateFirst.id());
      assertThat(ids(f, JobSearch.all().withCreatedBetween(second, second)))
          .containsExactly(earlySecond.id());
      assertThat(ids(f, JobSearch.all().withCreatedBetween(second, null)))
          .containsExactly(earlySecond.id());
      assertThat(ids(f, JobSearch.all().withCreatedBetween(null, first)))
          .containsExactly(lateFirst.id());
    }
  }

  private static List<Long> ids(JobFixtures f, JobSearch search) {
    return f.jobs.search(search).stream().map(JobSummary::id).toList();
  }
}
