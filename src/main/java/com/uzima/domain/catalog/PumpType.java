package com.uzima.domain.catalog;

import com.uzima.domain.money.Money;

/** Pump model and its base cost, before the tiered lift-height charge. */
public record PumpType(long id, String name, Money baseCost, boolean active)
    implements CatalogItem {

  public PumpType {
    CatalogItem.validate(name, baseCost, "baseCost");
    name = name.strip();
  }

  @Override
  public String displayName() {
    return name;
  }

  @Override
  public Money price() {
    return baseCost;
  }

  public PumpType withPrice(Money newPrice) {
    return new PumpType(id, name, newPrice, active);
  }

  public PumpType retired() {
    return new PumpType(id, name, baseCost, false);
  }
}
