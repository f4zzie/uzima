package com.uzima.pricing;

import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.common.Metres;
import com.uzima.domain.money.Money;
import java.util.Objects;

/** The metres that fall inside one rate tier, and what they cost. */
public record TierSegment(RateTier tier, Metres metres, Money amount) {

  public TierSegment {
    Objects.requireNonNull(tier, "tier");
    Objects.requireNonNull(metres, "metres");
    Objects.requireNonNull(amount, "amount");
  }
}
