package com.uzima.domain.job;

import com.uzima.domain.common.Metres;
import com.uzima.domain.money.Money;
import java.util.Objects;

/**
 * The per-metre part of a quote for one service (drilling depth or pump lift height), summed over
 * its rate-tier lines, with the working that produced it, e.g. {@code "100 m × 1,000 + 50 m ×
 * 1,500"}.
 */
public record MetreCharge(Metres metres, Money total, String working) {

  public MetreCharge {
    Objects.requireNonNull(metres, "metres");
    Objects.requireNonNull(total, "total");
    Objects.requireNonNull(working, "working");
  }
}
