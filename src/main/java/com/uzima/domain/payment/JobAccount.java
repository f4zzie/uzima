package com.uzima.domain.payment;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.common.Validator;
import com.uzima.domain.money.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * The payment position of one job, derived from its grand total and its ledger entries. Nothing
 * here is stored: amount paid, balance, status and refund due are always recomputed.
 *
 * <p>A cancelled job owes nothing. Money still held for it is a refund due until reversals bring
 * the net paid back to zero.
 */
public record JobAccount(Money grandTotal, boolean cancelled, List<Payment> payments) {

  public JobAccount {
    Objects.requireNonNull(grandTotal, "grandTotal");
    payments = List.copyOf(payments);
  }

  /** Total received minus total reversed. */
  public Money netPaid() {
    return Money.sum(payments.stream().map(Payment::amount));
  }

  /** What the client still owes; always zero for a cancelled job. */
  public Money balance() {
    return cancelled ? Money.ZERO : grandTotal.minus(netPaid());
  }

  public PaymentStatus status() {
    Money paid = netPaid();
    if (!paid.isPositive()) {
      return PaymentStatus.NOT_PAID;
    }
    return paid.isLessThan(grandTotal) ? PaymentStatus.PARTIALLY_PAID : PaymentStatus.PAID;
  }

  /** Money to hand back on a cancelled job; zero otherwise. */
  public Money refundDue() {
    Money paid = netPaid();
    return cancelled && paid.isPositive() ? paid : Money.ZERO;
  }

  /** True when the job counts towards outstanding balances and aged receivables. */
  public boolean isOutstanding() {
    return !cancelled && balance().isPositive();
  }

  /**
   * Checks that a new payment can be recorded.
   *
   * @throws DomainException if the job is cancelled
   * @throws ValidationException listing every invalid field otherwise
   */
  public void checkCanReceive(Money amount, LocalDate date, LocalDate today) {
    if (cancelled) {
      throw new DomainException("Payments cannot be recorded against a cancelled job");
    }
    Validator v = new Validator();
    if (amount == null || !amount.isPositive()) {
      v.add("amount", "Payment amount must be greater than zero");
    } else if (amount.isGreaterThan(balance())) {
      v.add(
          "amount",
          "This payment of "
              + amount.format()
              + " is more than the balance of "
              + balance().format());
    }
    if (date == null) {
      v.add("date", "Enter the payment date");
    } else if (date.isAfter(today)) {
      v.add("date", "Payment date cannot be in the future");
    }
    v.throwIfInvalid();
  }

  /**
   * Checks that {@code magnitude} (a positive amount) of {@code original} can be reversed. Partial
   * reversals are allowed until the original is fully reversed. Allowed on cancelled jobs, where a
   * reversal records a refund.
   */
  public void checkCanReverse(
      Payment.Received original, Money magnitude, String reason, LocalDate date) {
    Objects.requireNonNull(original, "original");
    if (original.id() <= 0
        || payments.stream()
            .noneMatch(p -> p instanceof Payment.Received r && r.id() == original.id())) {
      throw new DomainException("That payment does not belong to this job");
    }
    Money left = remainingToReverse(original);
    Validator v = new Validator();
    if (reason == null || reason.isBlank()) {
      v.add("reason", "Give a reason for the reversal");
    }
    if (magnitude == null || !magnitude.isPositive()) {
      v.add("amount", "Reversal amount must be greater than zero");
    } else if (left.isZero()) {
      v.add("amount", "This payment has already been fully reversed");
    } else if (magnitude.isGreaterThan(left)) {
      v.add("amount", "Only " + left.format() + " of this payment is left to reverse");
    }
    if (date == null) {
      v.add("date", "Enter the reversal date");
    } else if (date.isBefore(original.date())) {
      v.add("date", "A reversal cannot be dated before the payment it reverses");
    }
    v.throwIfInvalid();
  }

  /** The part of {@code original} not yet cancelled by earlier reversals. */
  public Money remainingToReverse(Payment.Received original) {
    Money reversed =
        Money.sum(
            payments.stream()
                .filter(
                    p -> p instanceof Payment.Reversal r && r.reversedPaymentId() == original.id())
                .map(Payment::amount));
    return original.amount().plus(reversed);
  }
}
