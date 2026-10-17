package com.uzima.pricing;

import com.uzima.domain.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The tier-by-tier breakdown of a metre-based charge. Zero metres has no segments. */
public record TieredCost(List<TierSegment> segments, Money total) {

  public TieredCost {
    segments = List.copyOf(segments);
    Objects.requireNonNull(total, "total");
  }

  /**
   * Human-readable working, e.g. {@code "100 m × 1,000 + 50 m × 1,500"}. Whole-shilling rates are
   * shown without decimals. Returns an empty string when there are no segments (zero metres).
   */
  public String describe() {
    return segments.stream()
        .map(s -> s.metres() + " × " + rate(s.tier().ratePerMetre()))
        .collect(Collectors.joining(" + "));
  }

  private static String rate(Money rate) {
    String plain = rate.formatPlain();
    return rate.amount().remainder(BigDecimal.ONE).signum() == 0
        ? plain.substring(0, plain.length() - 3)
        : plain;
  }
}
