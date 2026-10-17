package com.uzima.domain.payment;

import static com.uzima.domain.payment.PaymentFixtures.CLERK;
import static com.uzima.domain.payment.PaymentFixtures.DAY;
import static com.uzima.domain.payment.PaymentFixtures.JOB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.time.Year;
import org.junit.jupiter.api.Test;

class PaymentTest {

  private static final ReceiptNumber RCP = new ReceiptNumber(Year.of(2026), 1);

  private static Payment.Received received(Money amount, PaymentMethod method, String ref) {
    return new Payment.Received(0, RCP, JOB, amount, DAY, method, ref, CLERK, null);
  }

  private static Payment.Reversal reversal(Money amount, String reason) {
    return new Payment.Reversal(
        0, RCP, JOB, amount, DAY, PaymentMethod.MPESA, null, CLERK, null, 5, reason);
  }

  @Test
  void receivedNormalisesReferenceAndNotes() {
    Payment p = received(Money.of(1000), PaymentMethod.MPESA, "qjk3h5l7pz");
    assertThat(p.reference()).isEqualTo("QJK3H5L7PZ");
    assertThat(p.notes()).isEmpty();
  }

  @Test
  void receivedAmountMustBePositive() {
    assertThatThrownBy(() -> received(Money.ZERO, PaymentMethod.CASH, ""))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("amount")).isPresent());
    assertThatThrownBy(() -> received(Money.of(-5), PaymentMethod.CASH, ""))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void receivedRequiresMethodReference() {
    assertThatThrownBy(() -> received(Money.of(10), PaymentMethod.CHEQUE, ""))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("reference")).isPresent());
  }

  @Test
  void receivedRequiresDate() {
    assertThatThrownBy(
            () ->
                new Payment.Received(
                    0, RCP, JOB, Money.of(1), null, PaymentMethod.CASH, "", CLERK, ""))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("date")).isPresent());
  }

  @Test
  void notesAreLimited() {
    String longNotes = "n".repeat(Payment.MAX_NOTES + 1);
    assertThatThrownBy(
            () ->
                new Payment.Received(
                    0, RCP, JOB, Money.of(1), DAY, PaymentMethod.CASH, "", CLERK, longNotes))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("notes")).isPresent());
  }

  @Test
  void reversalIsNegativeWithReasonAndOptionalReference() {
    Payment.Reversal r = reversal(Money.of(-500), " duplicate ");
    assertThat(r.amount()).isEqualTo(Money.of(-500));
    assertThat(r.reason()).isEqualTo("duplicate");
    assertThat(r.reference()).isEmpty();
    assertThat(r.reversedPaymentId()).isEqualTo(5);
  }

  @Test
  void reversalRequiresReason() {
    assertThatThrownBy(() -> reversal(Money.of(-500), "  "))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("reason")).isPresent());
  }

  @Test
  void reversalAmountMustBeNegative() {
    assertThatThrownBy(() -> reversal(Money.of(500), "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void reversalMustLinkToSavedPayment() {
    assertThatThrownBy(
            () ->
                new Payment.Reversal(
                    0, RCP, JOB, Money.of(-1), DAY, PaymentMethod.CASH, "", CLERK, "", 0, "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void sealedHierarchySupportsExhaustiveSwitch() {
    Payment p = reversal(Money.of(-1), "x");
    String kind =
        switch (p) {
          case Payment.Received r -> "received";
          case Payment.Reversal r -> "reversal of " + r.reversedPaymentId();
        };
    assertThat(kind).isEqualTo("reversal of 5");
  }
}
