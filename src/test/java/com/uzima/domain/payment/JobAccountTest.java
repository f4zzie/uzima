package com.uzima.domain.payment;

import static com.uzima.domain.payment.PaymentFixtures.DAY;
import static com.uzima.domain.payment.PaymentFixtures.received;
import static com.uzima.domain.payment.PaymentFixtures.reversal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JobAccountTest {

  private static final Money TOTAL = Money.of(100_000);
  private static final LocalDate TODAY = DAY.plusDays(10);

  private static JobAccount open(Payment... payments) {
    return new JobAccount(TOTAL, false, List.of(payments));
  }

  private static JobAccount cancelled(Payment... payments) {
    return new JobAccount(TOTAL, true, List.of(payments));
  }

  private static ValidationException validationError(Runnable action) {
    try {
      action.run();
    } catch (ValidationException e) {
      return e;
    }
    throw new AssertionError("Expected ValidationException");
  }

  @Nested
  class Derivation {

    @Test
    void noPaymentsIsNotPaid() {
      JobAccount a = open();
      assertThat(a.netPaid()).isEqualTo(Money.ZERO);
      assertThat(a.balance()).isEqualTo(TOTAL);
      assertThat(a.status()).isEqualTo(PaymentStatus.NOT_PAID);
      assertThat(a.isOutstanding()).isTrue();
      assertThat(a.refundDue()).isEqualTo(Money.ZERO);
    }

    @Test
    void someMoneyIsPartiallyPaid() {
      JobAccount a = open(received(1, 30_000), received(2, 20_000));
      assertThat(a.netPaid()).isEqualTo(Money.of(50_000));
      assertThat(a.balance()).isEqualTo(Money.of(50_000));
      assertThat(a.status()).isEqualTo(PaymentStatus.PARTIALLY_PAID);
      assertThat(a.isOutstanding()).isTrue();
    }

    @Test
    void fullAmountIsPaid() {
      JobAccount a = open(received(1, 60_000), received(2, 40_000));
      assertThat(a.balance()).isEqualTo(Money.ZERO);
      assertThat(a.status()).isEqualTo(PaymentStatus.PAID);
      assertThat(a.isOutstanding()).isFalse();
    }

    @Test
    void reversalReducesPaidAndFlipsPaidToPartiallyPaid() {
      Payment.Received first = received(1, 60_000);
      JobAccount paid = open(first, received(2, 40_000));
      assertThat(paid.status()).isEqualTo(PaymentStatus.PAID);

      JobAccount reversed = open(first, received(2, 40_000), reversal(3, first, 10_000));
      assertThat(reversed.netPaid()).isEqualTo(Money.of(90_000));
      assertThat(reversed.balance()).isEqualTo(Money.of(10_000));
      assertThat(reversed.status()).isEqualTo(PaymentStatus.PARTIALLY_PAID);
    }

    @Test
    void fullyReversedIsNotPaid() {
      Payment.Received first = received(1, 60_000);
      JobAccount a = open(first, reversal(2, first, 60_000));
      assertThat(a.netPaid()).isEqualTo(Money.ZERO);
      assertThat(a.status()).isEqualTo(PaymentStatus.NOT_PAID);
    }
  }

  @Nested
  class Receiving {

    @Test
    void exactPayoffIsAllowed() {
      JobAccount a = open(received(1, 40_000));
      assertThatCode(() -> a.checkCanReceive(Money.of(60_000), DAY, TODAY))
          .doesNotThrowAnyException();
    }

    @Test
    void overpaymentIsRejectedWithClearMessage() {
      JobAccount a = open(received(1, 95_000));
      ValidationException e =
          validationError(() -> a.checkCanReceive(Money.of(10_000), DAY, TODAY));
      assertThat(e.messageFor("amount"))
          .contains("This payment of KSh 10,000.00 is more than the balance of KSh 5,000.00");
    }

    @Test
    void paymentOnFullyPaidJobIsOverpayment() {
      JobAccount a = open(received(1, 100_000));
      ValidationException e = validationError(() -> a.checkCanReceive(Money.of(1), DAY, TODAY));
      assertThat(e.messageFor("amount")).get().asString().contains("balance of KSh 0.00");
    }

    @Test
    void amountMustBePositive() {
      assertThat(
              validationError(() -> open().checkCanReceive(Money.ZERO, DAY, TODAY))
                  .messageFor("amount"))
          .isPresent();
      assertThat(
              validationError(() -> open().checkCanReceive(null, DAY, TODAY)).messageFor("amount"))
          .isPresent();
    }

    @Test
    void dateCannotBeInTheFutureOrMissing() {
      assertThat(
              validationError(() -> open().checkCanReceive(Money.of(1), TODAY.plusDays(1), TODAY))
                  .messageFor("date"))
          .isPresent();
      assertThat(
              validationError(() -> open().checkCanReceive(Money.of(1), null, TODAY))
                  .messageFor("date"))
          .isPresent();
      assertThatCode(() -> open().checkCanReceive(Money.of(1), TODAY, TODAY))
          .doesNotThrowAnyException();
    }

    @Test
    void reportsEveryInvalidFieldAtOnce() {
      ValidationException e =
          validationError(() -> open().checkCanReceive(Money.ZERO, TODAY.plusDays(1), TODAY));
      assertThat(e.errors()).hasSize(2);
    }
  }

  @Nested
  class Reversing {

    @Test
    void partialReversalsAreAllowedUpToTheOriginal() {
      Payment.Received first = received(1, 60_000);
      JobAccount a = open(first, reversal(2, first, 20_000));
      assertThat(a.remainingToReverse(first)).isEqualTo(Money.of(40_000));
      assertThatCode(() -> a.checkCanReverse(first, Money.of(40_000), "Refund", DAY))
          .doesNotThrowAnyException();
    }

    @Test
    void overReversalIsRejectedWithRemainingAmount() {
      Payment.Received first = received(1, 60_000);
      JobAccount a = open(first, reversal(2, first, 20_000));
      ValidationException e =
          validationError(() -> a.checkCanReverse(first, Money.of("40000.01"), "x", DAY));
      assertThat(e.messageFor("amount"))
          .contains("Only KSh 40,000.00 of this payment is left to reverse");
    }

    @Test
    void fullyReversedPaymentCannotBeReversedAgain() {
      Payment.Received first = received(1, 60_000);
      JobAccount a = open(first, reversal(2, first, 60_000));
      ValidationException e =
          validationError(() -> a.checkCanReverse(first, Money.of(1), "x", DAY));
      assertThat(e.messageFor("amount")).contains("This payment has already been fully reversed");
    }

    @Test
    void reversalsOfOtherPaymentsDoNotCount() {
      Payment.Received first = received(1, 60_000);
      Payment.Received second = received(2, 40_000);
      JobAccount a = open(first, second, reversal(3, second, 40_000));
      assertThat(a.remainingToReverse(first)).isEqualTo(Money.of(60_000));
    }

    @Test
    void reasonAmountAndDateAreValidated() {
      Payment.Received first = received(1, 60_000);
      JobAccount a = open(first);
      ValidationException e =
          validationError(() -> a.checkCanReverse(first, Money.ZERO, " ", DAY.minusDays(1)));
      assertThat(e.messageFor("reason")).isPresent();
      assertThat(e.messageFor("amount")).isPresent();
      assertThat(e.messageFor("date")).isPresent();
      assertThat(
              validationError(() -> a.checkCanReverse(first, Money.of(1), "x", null))
                  .messageFor("date"))
          .isPresent();
    }

    @Test
    void paymentMustBelongToThisJob() {
      Payment.Received stranger = received(99, 1_000);
      assertThatThrownBy(
              () -> open(received(1, 5)).checkCanReverse(stranger, Money.of(1), "x", DAY))
          .isInstanceOf(DomainException.class)
          .isNotInstanceOf(ValidationException.class)
          .hasMessageContaining("does not belong");
      Payment.Received unsaved = received(0, 1_000);
      assertThatThrownBy(() -> open(unsaved).checkCanReverse(unsaved, Money.of(1), "x", DAY))
          .isInstanceOf(DomainException.class);
    }
  }

  @Nested
  class CancelledJob {

    @Test
    void owesNothingAndIsNotOutstanding() {
      JobAccount a = cancelled(received(1, 30_000));
      assertThat(a.balance()).isEqualTo(Money.ZERO);
      assertThat(a.isOutstanding()).isFalse();
      assertThat(a.refundDue()).isEqualTo(Money.of(30_000));
    }

    @Test
    void withoutPaymentsHasNoRefundDue() {
      assertThat(cancelled().refundDue()).isEqualTo(Money.ZERO);
      assertThat(cancelled().balance()).isEqualTo(Money.ZERO);
    }

    @Test
    void rejectsNewPayments() {
      assertThatThrownBy(() -> cancelled().checkCanReceive(Money.of(1), DAY, TODAY))
          .isInstanceOf(DomainException.class)
          .isNotInstanceOf(ValidationException.class)
          .hasMessage("Payments cannot be recorded against a cancelled job");
    }

    @Test
    void allowsReversalsAsRefunds() {
      Payment.Received first = received(1, 30_000);
      assertThatCode(() -> cancelled(first).checkCanReverse(first, Money.of(30_000), "Refund", DAY))
          .doesNotThrowAnyException();
    }

    @Test
    void refundDueReachesZeroAfterFullReversal() {
      Payment.Received first = received(1, 30_000);
      Payment.Received second = received(2, 20_000);
      JobAccount partlyRefunded = cancelled(first, second, reversal(3, first, 30_000));
      assertThat(partlyRefunded.refundDue()).isEqualTo(Money.of(20_000));

      JobAccount refunded =
          cancelled(first, second, reversal(3, first, 30_000), reversal(4, second, 20_000));
      assertThat(refunded.refundDue()).isEqualTo(Money.ZERO);
      assertThat(refunded.netPaid()).isEqualTo(Money.ZERO);
    }
  }
}
