package com.uzima.domain.job;

import com.uzima.domain.common.Metres;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

  /**
   * The per-metre lines of {@code category} (one per rate tier) summed into one charge with its
   * working; empty when the category has no metre lines, e.g. zero lift height.
   */
  public Optional<MetreCharge> metreCharge(LineCategory category) {
    List<QuoteLine> metreLines =
        lines.stream()
            .filter(l -> l.category() == category && l.unit() == QuantityUnit.METRE)
            .toList();
    if (metreLines.isEmpty()) {
      return Optional.empty();
    }
    BigDecimal metres =
        metreLines.stream().map(QuoteLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    String working =
        metreLines.stream()
            .map(l -> new Metres(l.quantity()) + " × " + l.unitPrice().formatCompact())
            .collect(Collectors.joining(" + "));
    return Optional.of(
        new MetreCharge(
            new Metres(metres), Money.sum(metreLines.stream().map(QuoteLine::lineTotal)), working));
  }

  /** The lines of {@code category} that are charged once, not per metre, in quote order. */
  public List<QuoteLine> fixedLines(LineCategory category) {
    return lines.stream()
        .filter(l -> l.category() == category && l.unit() != QuantityUnit.METRE)
        .toList();
  }

  /** Sum of the lines in one category, e.g. all drilling lines. */
  public Money totalFor(LineCategory category) {
    return Money.sum(
        lines.stream().filter(l -> l.category() == category).map(QuoteLine::lineTotal));
  }
}
