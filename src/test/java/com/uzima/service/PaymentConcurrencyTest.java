package com.uzima.service;

import static com.uzima.service.PaymentTestFixture.JOB_TOTAL;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.Job;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.persistence.TestDatabases;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Eight clerks try to record 30% of the same job at the same moment. Locking the job row means only
 * three can succeed; the rest see the reduced balance and are told they would overpay.
 */
class PaymentConcurrencyTest {

  private static final int THREADS = 8;

  @ParameterizedTest
  @EnumSource(TestDatabases.Kind.class)
  void concurrentPaymentsCannotOverpayAJob(TestDatabases.Kind kind) throws Exception {
    try (PaymentTestFixture f = new PaymentTestFixture(kind)) {
      Job job = f.job();
      Money share = JOB_TOTAL.times(new BigDecimal("0.30"));
      List<Payment.Received> recorded = Collections.synchronizedList(new ArrayList<>());
      AtomicInteger rejected = new AtomicInteger();

      ExecutorService pool = Executors.newFixedThreadPool(THREADS);
      try {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
          tasks.add(
              () -> {
                try {
                  recorded.add(
                      f.service.record(
                          job.id(), share, LocalDate.of(2026, 10, 1), PaymentMethod.CASH, "", ""));
                } catch (ValidationException overpayment) {
                  rejected.incrementAndGet();
                }
                return null;
              });
        }
        for (Future<Void> done : pool.invokeAll(tasks)) {
          done.get();
        }
      } finally {
        pool.shutdownNow();
      }

      assertThat(recorded).hasSize(3);
      assertThat(rejected).hasValue(THREADS - 3);
      assertThat(f.service.account(job.id()).netPaid())
          .isEqualTo(share.times(BigDecimal.valueOf(3)))
          .matches(paid -> !paid.isGreaterThan(JOB_TOTAL));
      assertThat(recorded.stream().map(p -> p.receiptNumber().sequence()).sorted())
          .containsExactly(1, 2, 3);
    }
  }
}
