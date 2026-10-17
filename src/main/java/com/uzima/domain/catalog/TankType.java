package com.uzima.domain.catalog;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.text.NumberFormat;
import java.util.Locale;

/** Storage tank sold at a flat price by capacity. */
public record TankType(long id, int capacityLitres, Money price, boolean active)
    implements CatalogItem {

  public TankType {
    if (capacityLitres <= 0) {
      throw new ValidationException(
          "capacityLitres", "Capacity must be a positive number of litres");
    }
    CatalogItem.validate("tank", price, "price");
  }

  /** "5,000 L". */
  @Override
  public String displayName() {
    return NumberFormat.getIntegerInstance(Locale.UK).format(capacityLitres) + " L";
  }

  public TankType withPrice(Money newPrice) {
    return new TankType(id, capacityLitres, newPrice, active);
  }

  public TankType retired() {
    return new TankType(id, capacityLitres, price, false);
  }
}
