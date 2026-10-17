package com.uzima.domain.job;

import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.util.List;
import java.util.Objects;

/**
 * A priced quote: the line snapshots plus totals and the tax rate used. Immutable, so a quote saved
 * with a job keeps its figures when catalog prices or the tax rate change later.
 */
public record Quote(
    List<QuoteLine> lines, Money subtotal, TaxRate taxRate, Money tax, Money grandTotal) {

  public Quote {
    lines = List.copyOf(lines);
    Objects.requireNonNull(subtotal, "subtotal");
    Objects.requireNonNull(taxRate, "taxRate");
    Objects.requireNonNull(tax, "tax");
    Objects.requireNonNull(grandTotal, "grandTotal");
    Money sum = Money.sum(lines.stream().map(QuoteLine::lineTotal));
    if (sum.compareTo(subtotal) != 0) {
      throw new IllegalArgumentException(
          "Subtotal " + subtotal + " does not equal the sum of lines " + sum);
    }
    if (subtotal.plus(tax).compareTo(grandTotal) != 0) {
      throw new IllegalArgumentException(
          "Grand total " + grandTotal + " does not equal subtotal + tax " + subtotal.plus(tax));
    }
  }

  /** Sum of the lines in one category, e.g. all drilling lines. */
  public Money totalFor(LineCategory category) {
    return Money.sum(
        lines.stream().filter(l -> l.category() == category).map(QuoteLine::lineTotal));
  }
}
