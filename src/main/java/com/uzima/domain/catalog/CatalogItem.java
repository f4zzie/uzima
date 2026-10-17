package com.uzima.domain.catalog;

import com.uzima.domain.common.Validator;
import com.uzima.domain.money.Money;

/**
 * A priced catalog entry. Items are retired ({@code active = false}) rather than deleted once a job
 * references them, so the history stays intact.
 */
public sealed interface CatalogItem permits DrillingType, PumpType, TankType, PipeType {

  /** Unsaved items have id 0. */
  long id();

  String displayName();

  /** The price this item contributes: down payment, base cost, flat price or cost per metre. */
  Money price();

  boolean active();

  static void validate(String name, Money price, String priceField) {
    new Validator()
        .require(name, "name", "Name is required")
        .check(name == null || name.strip().length() <= 80, "name", "Name is too long")
        .check(price != null, priceField, "Price is required")
        .check(price == null || !price.isNegative(), priceField, "Price cannot be negative")
        .throwIfInvalid();
  }
}
