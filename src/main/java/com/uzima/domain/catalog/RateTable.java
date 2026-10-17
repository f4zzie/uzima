package com.uzima.domain.catalog;

import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import java.util.List;

/**
 * The ordered set of marginal per-metre tiers shared by borehole depth and pump lift height.
 *
 * <p>Shape rules, checked on construction (and so whenever tiers are saved): at least one tier; the
 * first starts at 0 m; each tier starts exactly where the previous one ends (contiguous, no
 * overlap); only the last tier is open-ended, and it must be.
 */
public record RateTable(List<RateTier> tiers) {

  public RateTable {
    tiers = List.copyOf(tiers);
    if (tiers.isEmpty()) {
      throw new ValidationException("tiers", "Add at least one rate tier");
    }
    if (!tiers.getFirst().from().isZero()) {
      throw new ValidationException("tiers", "The first tier must start at 0 m");
    }
    for (int i = 0; i < tiers.size(); i++) {
      RateTier tier = tiers.get(i);
      boolean last = i == tiers.size() - 1;
      if (last && !tier.isOpenEnded()) {
        throw new ValidationException("tiers", "The last tier must have no upper limit");
      }
      if (!last) {
        if (tier.isOpenEnded()) {
          throw new ValidationException("tiers", "Only the last tier can have no upper limit");
        }
        Metres end = tier.to().orElseThrow();
        Metres nextStart = tiers.get(i + 1).from();
        if (nextStart.compareTo(end) != 0) {
          throw new ValidationException(
              "tiers",
              "Tier "
                  + (i + 2)
                  + " must start at "
                  + end
                  + " where tier "
                  + (i + 1)
                  + " ends (it starts at "
                  + nextStart
                  + ")");
        }
      }
    }
  }
}
