package com.uzima.pricing;

import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.util.List;

/** The reference catalog from the business rules (section 5), for pricing tests. */
final class PricingFixtures {

  static final RateTable STANDARD_RATES =
      new RateTable(
          List.of(
              RateTier.bounded(0, 100, 1_000),
              RateTier.bounded(100, 200, 1_500),
              RateTier.bounded(200, 300, 2_000),
              RateTier.openEnded(300, 2_500)));

  static final DrillingType SYMMETRIC = new DrillingType(1, "Symmetric", Money.of(130_000), true);
  static final DrillingType CORE = new DrillingType(2, "Core", Money.of(225_000), true);
  static final DrillingType GEO_TECHNICAL =
      new DrillingType(3, "Geo-technical", Money.of(335_000), true);

  static final PumpType SUBMERSIBLE =
      new PumpType(1, "Submersible electric", Money.of(90_000), true);
  static final PumpType SOLAR = new PumpType(2, "Solar", Money.of(65_000), true);
  static final PumpType HAND = new PumpType(3, "Hand", Money.of(30_000), true);

  static final TankType TANK_1000 = new TankType(1, 1_000, Money.of(15_000), true);
  static final TankType TANK_2000 = new TankType(2, 2_000, Money.of(28_000), true);
  static final TankType TANK_5000 = new TankType(3, 5_000, Money.of(65_000), true);
  static final TankType TANK_10000 = new TankType(4, 10_000, Money.of(125_000), true);

  static final PipeType PVC = new PipeType(1, "PVC", Money.of(500), true);
  static final PipeType HDPE = new PipeType(2, "HDPE", Money.of(800), true);
  static final PipeType GALVANIZED = new PipeType(3, "Galvanized", Money.of(1_200), true);

  static final TaxRate VAT_16 = TaxRate.of("0.16");

  private PricingFixtures() {}

  static CategoryFees fees(ClientCategory category) {
    return switch (category) {
      case INDUSTRIAL -> new CategoryFees(category, Money.of(20_000), Money.of(50_000));
      case COMMERCIAL -> new CategoryFees(category, Money.of(15_000), Money.of(30_000));
      case DOMESTIC -> new CategoryFees(category, Money.of(7_000), Money.of(10_000));
    };
  }

  static PricingContext context(ClientCategory category) {
    return new PricingContext(fees(category), STANDARD_RATES, VAT_16);
  }

  static PricingContext context(ClientCategory category, TaxRate rate) {
    return new PricingContext(fees(category), STANDARD_RATES, rate);
  }
}
