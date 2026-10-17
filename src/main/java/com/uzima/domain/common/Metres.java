package com.uzima.domain.common;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A non-negative length in metres with at most two decimal places, matching {@code DECIMAL(8,2)}.
 * Used for borehole depth, pump lift height and pipe length.
 */
public record Metres(BigDecimal value) implements Comparable<Metres> {

  // Declared before ZERO: static fields initialise in order and the constructor reads MAX.
  private static final BigDecimal MAX = new BigDecimal("999999.99");
  public static final Metres ZERO = new Metres(BigDecimal.ZERO);

  public Metres {
    Objects.requireNonNull(value, "value");
    if (value.signum() < 0) {
      throw new ValidationException("metres", "Metres cannot be negative");
    }
    if (value.stripTrailingZeros().scale() > 2) {
      throw new ValidationException("metres", "Metres can have at most two decimal places");
    }
    if (value.compareTo(MAX) > 0) {
      throw new ValidationException("metres", "Metres cannot exceed 999,999.99");
    }
    value = value.setScale(2);
  }

  public static Metres of(String value) {
    try {
      return new Metres(new BigDecimal(value.trim()));
    } catch (NumberFormatException e) {
      throw new ValidationException("metres", "Enter a number of metres, e.g. 120.5");
    }
  }

  public static Metres of(long value) {
    return new Metres(BigDecimal.valueOf(value));
  }

  public boolean isZero() {
    return value.signum() == 0;
  }

  public Metres minus(Metres other) {
    return new Metres(value.subtract(other.value));
  }

  public Metres min(Metres other) {
    return compareTo(other) <= 0 ? this : other;
  }

  @Override
  public int compareTo(Metres o) {
    return value.compareTo(o.value);
  }

  /** "150 m", "100.5 m". */
  @Override
  public String toString() {
    return value.stripTrailingZeros().toPlainString() + " m";
  }
}
