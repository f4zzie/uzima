package com.uzima.domain.payment;

import com.uzima.domain.common.ValidationException;
import java.time.Year;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Receipt number {@code RCP-{yyyy}-{NNNNN}}, sequential per year and shared by payments and
 * reversals. Numbers past 99999 widen.
 */
public record ReceiptNumber(Year year, int sequence) {

  private static final Pattern FORMAT = Pattern.compile("RCP-(\\d{4})-(\\d{5,9})");

  public ReceiptNumber {
    Objects.requireNonNull(year, "year");
    if (sequence < 1) {
      throw new IllegalArgumentException("Receipt sequence must be positive: " + sequence);
    }
  }

  public static ReceiptNumber parse(String text) {
    Matcher m = FORMAT.matcher(text == null ? "" : text.strip());
    if (!m.matches() || Integer.parseInt(m.group(2)) < 1) {
      throw new ValidationException("receiptNumber", "Receipt numbers look like RCP-2026-00042");
    }
    return new ReceiptNumber(Year.parse(m.group(1)), Integer.parseInt(m.group(2)));
  }

  public String value() {
    return "RCP-%s-%05d".formatted(year, sequence);
  }

  @Override
  public String toString() {
    return value();
  }
}
