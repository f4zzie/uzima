package com.uzima.domain.payment;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.time.LocalDate;
import java.util.Objects;

/**
 * An append-only entry in a job's payment ledger. Money received is a {@link Received}; a mistake
 * or refund is a {@link Reversal} with a negative amount linked to the payment it reverses. Entries
 * are never edited or deleted.
 */
public sealed interface Payment permits Payment.Received, Payment.Reversal {

  int MAX_NOTES = 500;

  /** 0 until saved. */
  long id();

  ReceiptNumber receiptNumber();

  long jobId();

  /** Positive for {@link Received}, negative for {@link Reversal}. */
  Money amount();

  LocalDate date();

  PaymentMethod method();

  String reference();

  /** The user who recorded the entry. */
  long receivedBy();

  String notes();

  /** Money received from the client. */
  record Received(
      long id,
      ReceiptNumber receiptNumber,
      long jobId,
      Money amount,
      LocalDate date,
      PaymentMethod method,
      String reference,
      long receivedBy,
      String notes)
      implements Payment {

    public Received {
      requireCommon(receiptNumber, date, method);
      Objects.requireNonNull(amount, "amount");
      if (!amount.isPositive()) {
        throw new ValidationException("amount", "Payment amount must be greater than zero");
      }
      reference = method.normaliseReference(reference);
      notes = cleanNotes(notes);
    }
  }

  /**
   * Cancels all or part of an earlier {@link Received} payment. Used to correct mistakes and to
   * record refunds. Uses the original payment's method; the reference is optional.
   */
  record Reversal(
      long id,
      ReceiptNumber receiptNumber,
      long jobId,
      Money amount,
      LocalDate date,
      PaymentMethod method,
      String reference,
      long receivedBy,
      String notes,
      long reversedPaymentId,
      String reason)
      implements Payment {

    public Reversal {
      requireCommon(receiptNumber, date, method);
      Objects.requireNonNull(amount, "amount");
      if (!amount.isNegative()) {
        throw new IllegalArgumentException("A reversal amount must be negative: " + amount);
      }
      if (reversedPaymentId <= 0) {
        throw new IllegalArgumentException("A reversal must reference a saved payment");
      }
      reason = reason == null ? "" : reason.strip();
      if (reason.isEmpty()) {
        throw new ValidationException("reason", "Give a reason for the reversal");
      }
      if (reason.length() > MAX_NOTES) {
        throw new ValidationException("reason", "Reason is too long");
      }
      // A refund leaves a trail just like a payment: M-Pesa, bank and cheque refunds need a
      // reference in the same format; cash refunds don't.
      reference = method.normaliseReference(reference);
      notes = cleanNotes(notes);
    }
  }

  private static void requireCommon(
      ReceiptNumber receiptNumber, LocalDate date, PaymentMethod method) {
    Objects.requireNonNull(receiptNumber, "receiptNumber");
    Objects.requireNonNull(method, "method");
    if (date == null) {
      throw new ValidationException("date", "Enter the payment date");
    }
  }

  private static String cleanNotes(String notes) {
    String cleaned = notes == null ? "" : notes.strip();
    if (cleaned.length() > MAX_NOTES) {
      throw new ValidationException("notes", "Notes can be at most " + MAX_NOTES + " characters");
    }
    return cleaned;
  }
}
