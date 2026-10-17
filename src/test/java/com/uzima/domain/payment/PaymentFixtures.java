package com.uzima.domain.payment;

import com.uzima.domain.money.Money;
import java.time.LocalDate;
import java.time.Year;

/** Builders for payment ledger entries in tests. */
final class PaymentFixtures {

  static final long JOB = 10;
  static final long CLERK = 3;
  static final LocalDate DAY = LocalDate.of(2026, 10, 1);

  private static int nextReceipt = 1;

  private PaymentFixtures() {}

  static Payment.Received received(long id, long shillings) {
    return received(id, shillings, DAY);
  }

  static Payment.Received received(long id, long shillings, LocalDate date) {
    return new Payment.Received(
        id, receipt(), JOB, Money.of(shillings), date, PaymentMethod.CASH, "", CLERK, "");
  }

  static Payment.Reversal reversal(long id, Payment.Received original, long shillings) {
    return new Payment.Reversal(
        id,
        receipt(),
        JOB,
        Money.of(shillings).negate(),
        original.date(),
        original.method(),
        "",
        CLERK,
        "",
        original.id(),
        "Keyed twice");
  }

  private static ReceiptNumber receipt() {
    return new ReceiptNumber(Year.of(2026), nextReceipt++);
  }
}
