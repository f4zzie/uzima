package com.uzima.domain.money;

import com.uzima.domain.common.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An amount of Kenyan Shillings, always at scale 2. Every operation that can produce more than two
 * decimal places rounds {@link RoundingMode#HALF_UP}. Amounts may be negative (payment reversals).
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

  public static final String CURRENCY = "KES";

  // Must be initialised before ZERO: the constructor reads them.
  static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
  private static final int SCALE = 2;

  public static final Money ZERO = new Money(BigDecimal.ZERO);

  public Money {
    Objects.requireNonNull(amount, "amount");
    amount = amount.setScale(SCALE, ROUNDING);
  }

  public static Money of(long shillings) {
    return new Money(BigDecimal.valueOf(shillings));
  }

  public static Money of(String amount) {
    return new Money(new BigDecimal(amount));
  }

  /**
   * Parses user input such as {@code "1,234.50"} or {@code "KSh 1,234.50"}. Rejects more than two
   * decimal places instead of silently rounding what the user typed.
   */
  public static Money parse(String text, String field) {
    if (text == null || text.isBlank()) {
      throw new ValidationException(field, "Enter an amount");
    }
    String cleaned = text.trim().replaceFirst("(?i)^ksh\\.?\\s*", "").replace(",", "").trim();
    BigDecimal value;
    try {
      value = new BigDecimal(cleaned);
    } catch (NumberFormatException e) {
      throw new ValidationException(field, "Enter an amount like 1,234.50");
    }
    if (value.stripTrailingZeros().scale() > SCALE) {
      throw new ValidationException(field, "Amounts can have at most two decimal places");
    }
    return new Money(value);
  }

  public static Money sum(Stream<Money> amounts) {
    return amounts.reduce(ZERO, Money::plus);
  }

  public Money plus(Money other) {
    return new Money(amount.add(other.amount));
  }

  public Money minus(Money other) {
    return new Money(amount.subtract(other.amount));
  }

  public Money negate() {
    return new Money(amount.negate());
  }

  /** Multiplies by a quantity or rate and rounds the result HALF_UP to cents. */
  public Money times(BigDecimal factor) {
    return new Money(amount.multiply(factor));
  }

  /** Divides and rounds HALF_UP to cents. */
  public Money dividedBy(BigDecimal divisor) {
    return new Money(amount.divide(divisor, SCALE, ROUNDING));
  }

  public boolean isZero() {
    return amount.signum() == 0;
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  public boolean isNegative() {
    return amount.signum() < 0;
  }

  public boolean isGreaterThan(Money other) {
    return compareTo(other) > 0;
  }

  public boolean isLessThan(Money other) {
    return compareTo(other) < 0;
  }

  public Money max(Money other) {
    return compareTo(other) >= 0 ? this : other;
  }

  public Money min(Money other) {
    return compareTo(other) <= 0 ? this : other;
  }

  @Override
  public int compareTo(Money o) {
    return amount.compareTo(o.amount);
  }

  /** {@code KSh 1,234,567.00}; negative amounts as {@code -KSh 1,000.00}. */
  public String format() {
    String digits = number().format(amount.abs());
    return (isNegative() ? "-" : "") + "KSh " + digits;
  }

  /** {@code 1,234,567.00} without the currency label, for table columns. */
  public String formatPlain() {
    return number().format(amount);
  }

  @Override
  public String toString() {
    return format();
  }

  private static DecimalFormat number() {
    // DecimalFormat is not thread-safe, so build one per call. Symbols are fixed, not
    // locale-dependent, so output is identical on every machine.
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator(',');
    symbols.setDecimalSeparator('.');
    symbols.setMinusSign('-');
    DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
    format.setRoundingMode(ROUNDING);
    return format;
  }
}
