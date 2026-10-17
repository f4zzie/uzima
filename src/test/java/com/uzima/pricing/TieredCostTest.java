package com.uzima.pricing;

import static com.uzima.pricing.PricingFixtures.STANDARD_RATES;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.common.Metres;
import com.uzima.domain.money.Money;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TieredCostTest {

  private final PricingEngine engine = new PricingEngine();

  /** Acceptance example 4: tier boundaries with the standard table. */
  @ParameterizedTest(name = "{0} m costs {1}")
  @CsvSource({
    "0,      0.00",
    "100,    100000.00",
    "100.5,  100750.00",
    "200,    250000.00",
    "300,    450000.00",
    "300.01, 450025.00",
  })
  void tierBoundaries(String metres, String expected) {
    assertThat(engine.tieredCost(Metres.of(metres), STANDARD_RATES).total())
        .isEqualTo(Money.of(expected));
  }

  @Test
  void marginalNotFlat() {
    TieredCost cost = engine.tieredCost(Metres.of(150), STANDARD_RATES);
    assertThat(cost.total()).isEqualTo(Money.of("175000.00"));
    assertThat(cost.segments()).hasSize(2);
  }

  @Test
  void describeShowsWorking() {
    assertThat(engine.tieredCost(Metres.of(150), STANDARD_RATES).describe())
        .isEqualTo("100 m × 1,000 + 50 m × 1,500");
    assertThat(engine.tieredCost(Metres.of("350"), STANDARD_RATES).describe())
        .isEqualTo("100 m × 1,000 + 100 m × 1,500 + 100 m × 2,000 + 50 m × 2,500");
    assertThat(engine.tieredCost(Metres.of("100.5"), STANDARD_RATES).describe())
        .isEqualTo("100 m × 1,000 + 0.5 m × 1,500");
  }

  @Test
  void describeIsEmptyForZeroMetres() {
    assertThat(engine.tieredCost(Metres.ZERO, STANDARD_RATES).describe()).isEmpty();
  }

  @Test
  void describeKeepsCentsInRates() {
    RateTable rates = new RateTable(List.of(openEnded(0, "333.33")));
    assertThat(engine.tieredCost(Metres.of(2), rates).describe()).isEqualTo("2 m × 333.33");
  }

  @Test
  void singleOpenEndedTierIsAFlatRate() {
    RateTable flat = new RateTable(List.of(RateTier.openEnded(0, 1_200)));
    assertThat(engine.tieredCost(Metres.of("1000.25"), flat).total())
        .isEqualTo(Money.of("1200300.00"));
  }

  @Test
  void customTwoTierTableWithCentRates() {
    RateTable rates =
        new RateTable(
            List.of(
                new RateTier(Metres.ZERO, Optional.of(Metres.of(50)), Money.of("333.33")),
                RateTier.openEnded(50, 1_000)));

    // 50 × 333.33 = 16,666.50; 10.5 × 1,000 = 10,500.00
    assertThat(engine.tieredCost(Metres.of("60.5"), rates).total()).isEqualTo(Money.of("27166.50"));
  }

  @Test
  void eachSegmentRoundsHalfUpToCents() {
    RateTable rates = new RateTable(List.of(openEnded(0, "333.33")));
    // 0.15 × 333.33 = 49.9995 → 50.00
    assertThat(engine.tieredCost(Metres.of("0.15"), rates).total()).isEqualTo(Money.of("50.00"));
  }

  @Test
  void unevenCustomBandsAreHonoured() {
    RateTable rates =
        new RateTable(
            List.of(
                RateTier.bounded(0, 30, 800),
                RateTier.bounded(30, 75, 1_100),
                RateTier.openEnded(75, 3_000)));

    // 30 × 800 + 45 × 1,100 + 5 × 3,000 = 24,000 + 49,500 + 15,000
    assertThat(engine.tieredCost(Metres.of(80), rates).total()).isEqualTo(Money.of("88500.00"));
  }

  private static RateTier openEnded(long from, String rate) {
    return new RateTier(Metres.of(from), Optional.empty(), Money.of(rate));
  }
}
