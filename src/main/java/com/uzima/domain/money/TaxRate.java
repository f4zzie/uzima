package com.uzima.domain.money;

import com.uzima.domain.common.ValidationException;
import java.math.BigDecimal;
import java.util.Objects;

/** A VAT rate as a fraction, e.g. {@code 0.16}. Stored with up to four decimal places. */
public record TaxRate(BigDecimal value) {

  public static final TaxRate KENYA_STANDARD = new TaxRate(new BigDecimal("0.16"));
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  public TaxRate {
    Objects.requireNonNull(value, "value");
    if (value.signum() < 0 || value.compareTo(BigDecimal.ONE) >= 0) {
      throw new ValidationException("taxRate", "Tax rate must be between 0% and 99.99%");
    }
    if (value.stripTrailingZeros().scale() > 4) {
      throw new ValidationException("taxRate", "Tax rate can have at most two decimal places");
    }
    value = value.setScale(4);
  }

  public static TaxRate of(String fraction) {
    return new TaxRate(new BigDecimal(fraction));
  }

  /** Parses a percentage as typed in Settings, e.g. {@code "16"} or {@code "16%"}. */
  public static TaxRate parsePercent(String text) {
    try {
      BigDecimal percent = new BigDecimal(text.trim().replace("%", "").trim());
      return new TaxRate(percent.divide(HUNDRED));
    } catch (NumberFormatException | NullPointerException e) {
      throw new ValidationException("taxRate", "Enter the tax rate as a percentage, e.g. 16");
    }
  }

  /** "16%", "14.5%". */
  public String formatPercent() {
    return value.multiply(HUNDRED).stripTrailingZeros().toPlainString() + "%";
  }

  @Override
  public String toString() {
    return formatPercent();
  }
}
