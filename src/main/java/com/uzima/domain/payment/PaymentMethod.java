package com.uzima.domain.payment;

import com.uzima.domain.common.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/** How a payment was made, and what reference each method requires. */
public enum PaymentMethod {
  CASH("Cash"),
  MPESA("M-Pesa"),
  BANK_TRANSFER("Bank transfer"),
  CHEQUE("Cheque");

  public static final String FIELD = "reference";
  public static final int MAX_REFERENCE = 64;

  /** M-Pesa receipt codes are ten upper-case letters and digits, e.g. {@code QJK3H5L7PZ}. */
  private static final Pattern MPESA_RECEIPT = Pattern.compile("[A-Z0-9]{10}");

  private final String label;

  PaymentMethod(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  /**
   * Validates and cleans a payment reference for this method. M-Pesa codes are upper-cased because
   * people often type them in lower case. A blank cash reference becomes {@code ""}.
   */
  public String normaliseReference(String raw) {
    String ref = raw == null ? "" : raw.strip();
    switch (this) {
      case MPESA -> {
        ref = ref.toUpperCase(Locale.ROOT);
        if (ref.isEmpty()) {
          throw new ValidationException(FIELD, "Enter the M-Pesa receipt code");
        }
        if (!MPESA_RECEIPT.matcher(ref).matches()) {
          throw new ValidationException(
              FIELD, "M-Pesa receipt codes are 10 letters and digits, e.g. QJK3H5L7PZ");
        }
      }
      case BANK_TRANSFER, CHEQUE -> {
        if (ref.isEmpty()) {
          throw new ValidationException(
              FIELD,
              this == CHEQUE ? "Enter the cheque number" : "Enter the bank transfer reference");
        }
        checkLength(ref);
      }
      case CASH -> checkLength(ref);
    }
    return ref;
  }

  private static void checkLength(String ref) {
    if (ref.length() > MAX_REFERENCE) {
      throw new ValidationException(
          FIELD, "Reference can be at most " + MAX_REFERENCE + " characters");
    }
  }

  @Override
  public String toString() {
    return label;
  }
}
