package com.uzima.domain.job;

import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.money.TaxRate;
import java.util.Objects;

/**
 * The fees, per-metre rate tiers and tax rate a quote was priced with. A job stores its basis as
 * part of the price snapshot, so later edits to a pending job are priced the same way even after
 * Settings or the catalog change.
 */
public record PriceBasis(CategoryFees fees, RateTable rates, TaxRate taxRate) {

  public PriceBasis {
    Objects.requireNonNull(fees, "fees");
    Objects.requireNonNull(rates, "rates");
    Objects.requireNonNull(taxRate, "taxRate");
  }
}
