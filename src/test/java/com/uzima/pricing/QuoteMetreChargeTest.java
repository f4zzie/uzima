package com.uzima.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.MetreCharge;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** A quote describes its per-metre charges itself, so screens and documents never add them up. */
class QuoteMetreChargeTest {

  private static final RateTable RATES =
      new RateTable(
          List.of(
              new RateTier(Metres.ZERO, Optional.of(Metres.of(100)), Money.of(1_000)),
              new RateTier(Metres.of(100), Optional.of(Metres.of(200)), Money.of(1_500)),
              new RateTier(Metres.of(200), Optional.of(Metres.of(300)), Money.of(2_000)),
              new RateTier(Metres.of(300), Optional.empty(), Money.of(2_500))));
  private static final PriceBasis BASIS =
      new PriceBasis(
          new CategoryFees(ClientCategory.INDUSTRIAL, Money.of(20_000), Money.of(50_000)),
          RATES,
          TaxRate.of("0.16"));
  private static final DrillingType SYMMETRIC =
      new DrillingType(1, "Symmetric", Money.of(130_000), true);
  private static final PumpType SUBMERSIBLE =
      new PumpType(1, "Submersible electric", Money.of(90_000), true);

  private final PricingEngine engine = new PricingEngine();

  private Quote quote(String depth, String lift) {
    return engine.quote(
        new QuoteSpec(
            Optional.of(new DrillingSpec(SYMMETRIC, Metres.of(depth))),
            Optional.of(new PumpSpec(SUBMERSIBLE, Metres.of(lift))),
            Optional.empty(),
            Optional.empty()),
        BASIS);
  }

  @Test
  void drillingDepthIsSummedAcrossTiersWithItsWorking() {
    MetreCharge drilling = quote("150", "10").metreCharge(LineCategory.DRILLING).orElseThrow();

    assertThat(drilling.metres()).isEqualTo(Metres.of(150));
    assertThat(drilling.total()).isEqualTo(Money.of(175_000));
    assertThat(drilling.working()).isEqualTo("100 m × 1,000 + 50 m × 1,500");
  }

  @Test
  void pumpLiftInOneTier() {
    MetreCharge pump = quote("150", "10").metreCharge(LineCategory.PUMP).orElseThrow();

    assertThat(pump.metres()).isEqualTo(Metres.of(10));
    assertThat(pump.total()).isEqualTo(Money.of(10_000));
    assertThat(pump.working()).isEqualTo("10 m × 1,000");
  }

  @Test
  void fractionalMetresKeepTheirDecimals() {
    assertThat(quote("100.5", "0").metreCharge(LineCategory.DRILLING).orElseThrow().working())
        .isEqualTo("100 m × 1,000 + 0.5 m × 1,500");
  }

  @Test
  void zeroLiftHasNoMetreCharge() {
    assertThat(quote("80", "0").metreCharge(LineCategory.PUMP)).isEmpty();
    assertThat(quote("80", "0").metreCharge(LineCategory.TANK)).isEmpty();
  }

  @Test
  void fixedLinesLeaveOutTheTierLines() {
    Quote quote = quote("150", "10");
    assertThat(quote.fixedLines(LineCategory.DRILLING))
        .extracting(l -> l.description())
        .containsExactly("Drilling down payment (Symmetric)");
    assertThat(quote.fixedLines(LineCategory.SURVEY_FEE)).hasSize(1);
  }
}
