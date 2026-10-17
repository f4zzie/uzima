package com.uzima.domain.catalog;

import com.uzima.domain.money.Money;

/** Drilling method and its fixed down payment. */
public record DrillingType(long id, String name, Money downPayment, boolean active)
    implements CatalogItem {

  public DrillingType {
    CatalogItem.validate(name, downPayment, "downPayment");
    name = name.strip();
  }

  @Override
  public String displayName() {
    return name;
  }

  @Override
  public Money price() {
    return downPayment;
  }

  public DrillingType withPrice(Money newPrice) {
    return new DrillingType(id, name, newPrice, active);
  }

  public DrillingType retired() {
    return new DrillingType(id, name, downPayment, false);
  }
}
