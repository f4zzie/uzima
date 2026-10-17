package com.uzima.pricing;

import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.money.TaxRate;
import java.util.Objects;

/**
 * The live prices a quote is calculated against: the client category's fees, the per-metre rate
 * tiers and the current VAT rate.
 */
public record PricingContext(CategoryFees fees, RateTable rates, TaxRate taxRate) {

  public PricingContext {
    Objects.requireNonNull(fees, "fees");
    Objects.requireNonNull(rates, "rates");
    Objects.requireNonNull(taxRate, "taxRate");
  }
}
