package com.uzima.domain.catalog;

import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.util.Objects;
import java.util.Optional;

/**
 * One marginal band of the per-metre rate: metres in {@code (from, to]} cost {@code ratePerMetre}.
 * An empty {@code to} means the band is open-ended.
 */
public record RateTier(Metres from, Optional<Metres> to, Money ratePerMetre) {

  public RateTier {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    Objects.requireNonNull(ratePerMetre, "ratePerMetre");
    if (!ratePerMetre.isPositive()) {
      throw new ValidationException("ratePerMetre", "Rate per metre must be greater than zero");
    }
    if (to.isPresent() && to.get().compareTo(from) <= 0) {
      throw new ValidationException("to", "Each tier must end after it starts");
    }
  }

  public static RateTier bounded(long from, long to, long rate) {
    return new RateTier(Metres.of(from), Optional.of(Metres.of(to)), Money.of(rate));
  }

  public static RateTier openEnded(long from, long rate) {
    return new RateTier(Metres.of(from), Optional.empty(), Money.of(rate));
  }

  public boolean isOpenEnded() {
    return to.isEmpty();
  }

  /** "0–100 m", "300 m and above". */
  public String label() {
    return to.map(t -> from.value().stripTrailingZeros().toPlainString() + "–" + t)
        .orElse(from + " and above");
  }
}
