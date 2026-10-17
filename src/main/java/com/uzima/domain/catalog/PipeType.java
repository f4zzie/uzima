package com.uzima.domain.catalog;

import com.uzima.domain.money.Money;

/** Pipe material and its cost per metre. */
public record PipeType(long id, String name, Money costPerMetre, boolean active)
    implements CatalogItem {

  public PipeType {
    CatalogItem.validate(name, costPerMetre, "costPerMetre");
    name = name.strip();
  }

  @Override
  public String displayName() {
    return name;
  }

  @Override
  public Money price() {
    return costPerMetre;
  }

  public PipeType withPrice(Money newPrice) {
    return new PipeType(id, name, newPrice, active);
  }

  public PipeType retired() {
    return new PipeType(id, name, costPerMetre, false);
  }
}
