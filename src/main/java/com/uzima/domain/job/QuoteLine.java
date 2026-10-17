package com.uzima.domain.job;

import com.uzima.domain.money.Money;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * One priced line of a quote, stored with the job as a snapshot. Invoices and reports read these
 * values, never the live catalog, so later price changes cannot alter a saved job.
 */
public record QuoteLine(
    LineCategory category,
    String description,
    BigDecimal quantity,
    QuantityUnit unit,
    Money unitPrice,
    Money lineTotal) {

  public QuoteLine {
    Objects.requireNonNull(category, "category");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(quantity, "quantity");
    Objects.requireNonNull(unit, "unit");
    Objects.requireNonNull(unitPrice, "unitPrice");
    Objects.requireNonNull(lineTotal, "lineTotal");
    if (description.isBlank()) {
      throw new IllegalArgumentException("Quote line description is required");
    }
    if (quantity.signum() <= 0) {
      throw new IllegalArgumentException("Quote line quantity must be positive: " + quantity);
    }
    // Quantities are stored as DECIMAL(10,2); one canonical scale keeps saved and fresh lines
    // equal. Metres never have more than two decimals, so this never rounds.
    quantity = quantity.setScale(2);
  }
}
