package com.uzima.pricing;

import static com.uzima.pricing.PricingFixtures.GALVANIZED;
import static com.uzima.pricing.PricingFixtures.GEO_TECHNICAL;
import static com.uzima.pricing.PricingFixtures.HAND;
import static com.uzima.pricing.PricingFixtures.HDPE;
import static com.uzima.pricing.PricingFixtures.PVC;
import static com.uzima.pricing.PricingFixtures.SUBMERSIBLE;
import static com.uzima.pricing.PricingFixtures.SYMMETRIC;
import static com.uzima.pricing.PricingFixtures.TANK_1000;
import static com.uzima.pricing.PricingFixtures.TANK_5000;
import static com.uzima.pricing.PricingFixtures.context;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.PipeRun;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuantityUnit;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteLine;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PricingEngineTest {

  private final PricingEngine engine = new PricingEngine();

  private static Money ksh(String amount) {
    return Money.of(amount);
  }

  /** Acceptance examples from business rules section 5.8, verbatim. */
  @Nested
  class AcceptanceExamples {

    @Test
    void example1_industrialFullService() {
      QuoteSpec spec =
          new QuoteSpec(
              Optional.of(new DrillingSpec(SYMMETRIC, Metres.of(150))),
              Optional.of(new PumpSpec(SUBMERSIBLE, Metres.of(10))),
              Optional.of(TANK_5000),
              Optional.of(new Plumbing.Manual(Money.of(50_000), "Plumbing to house and trough")));

      Quote quote = engine.quote(spec, context(ClientCategory.INDUSTRIAL));

      assertThat(quote.totalFor(LineCategory.DRILLING)).isEqualTo(ksh("305000.00"));
      assertThat(quote.totalFor(LineCategory.PUMP)).isEqualTo(ksh("100000.00"));
      assertThat(quote.totalFor(LineCategory.TANK)).isEqualTo(ksh("65000.00"));
      assertThat(quote.totalFor(LineCategory.PLUMBING)).isEqualTo(ksh("50000.00"));
      assertThat(quote.totalFor(LineCategory.SURVEY_FEE)).isEqualTo(ksh("20000.00"));
      assertThat(quote.totalFor(LineCategory.AUTHORITY_FEE)).isEqualTo(ksh("50000.00"));
      assertThat(quote.subtotal()).isEqualTo(ksh("590000.00"));
      assertThat(quote.tax()).isEqualTo(ksh("94400.00"));
      assertThat(quote.grandTotal()).isEqualTo(ksh("684400.00"));
      assertThat(quote.taxRate()).isEqualTo(TaxRate.of("0.16"));
    }

    @Test
    void example2_domesticBasic() {
      QuoteSpec spec =
          new QuoteSpec(
              Optional.of(new DrillingSpec(SYMMETRIC, Metres.of(80))),
              Optional.of(new PumpSpec(HAND, Metres.ZERO)),
              Optional.empty(),
              Optional.empty());

      Quote quote = engine.quote(spec, context(ClientCategory.DOMESTIC));

      assertThat(quote.totalFor(LineCategory.DRILLING)).isEqualTo(ksh("210000.00"));
      assertThat(quote.totalFor(LineCategory.PUMP)).isEqualTo(ksh("30000.00"));
      assertThat(quote.subtotal()).isEqualTo(ksh("257000.00"));
      assertThat(quote.tax()).isEqualTo(ksh("41120.00"));
      assertThat(quote.grandTotal()).isEqualTo(ksh("298120.00"));
    }

    @Test
    void example3_commercialDeep() {
      QuoteSpec spec =
          new QuoteSpec(
              Optional.of(new DrillingSpec(GEO_TECHNICAL, Metres.of(350))),
              Optional.empty(),
              Optional.empty(),
              Optional.empty());

      Quote quote = engine.quote(spec, context(ClientCategory.COMMERCIAL));

      assertThat(quote.totalFor(LineCategory.DRILLING)).isEqualTo(ksh("910000.00"));
      assertThat(quote.subtotal()).isEqualTo(ksh("955000.00"));
      assertThat(quote.tax()).isEqualTo(ksh("152800.00"));
      assertThat(quote.grandTotal()).isEqualTo(ksh("1107800.00"));
    }

    @Test
    void example5_newTaxRateChangesNewQuotesButNotSavedOnes() {
      QuoteSpec spec =
          new QuoteSpec(
              Optional.of(new DrillingSpec(SYMMETRIC, Metres.of(80))),
              Optional.of(new PumpSpec(HAND, Metres.ZERO)),
              Optional.empty(),
              Optional.empty());
      Quote saved = engine.quote(spec, context(ClientCategory.DOMESTIC));

      Quote requoted = engine.quote(spec, context(ClientCategory.DOMESTIC, TaxRate.of("0.14")));

      assertThat(requoted.taxRate()).isEqualTo(TaxRate.of("0.14"));
      assertThat(requoted.tax()).isEqualTo(ksh("35980.00"));
      assertThat(requoted.grandTotal()).isEqualTo(ksh("292980.00"));
      assertThat(saved.taxRate()).isEqualTo(TaxRate.of("0.16"));
      assertThat(saved.tax()).isEqualTo(ksh("41120.00"));
      assertThat(saved.grandTotal()).isEqualTo(ksh("298120.00"));
    }
  }

  @Nested
  class Lines {

    @Test
    void drillingHasDownPaymentPlusOneMetreLinePerTierBand() {
      Quote quote =
          engine.quote(
              new QuoteSpec(
                  Optional.of(new DrillingSpec(SYMMETRIC, Metres.of(150))),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty()),
              context(ClientCategory.DOMESTIC));

      List<QuoteLine> drilling =
          quote.lines().stream().filter(l -> l.category() == LineCategory.DRILLING).toList();
      assertThat(drilling)
          .extracting(QuoteLine::description)
          .containsExactly(
              "Drilling down payment (Symmetric)",
              "Drilling, 0–100 m band",
              "Drilling, 100–200 m band");
      QuoteLine second = drilling.get(2);
      assertThat(second.quantity()).isEqualByComparingTo("50");
      assertThat(second.unit()).isEqualTo(QuantityUnit.METRE);
      assertThat(second.unitPrice()).isEqualTo(ksh("1500.00"));
      assertThat(second.lineTotal()).isEqualTo(ksh("75000.00"));
    }

    @Test
    void zeroMetresAddsNoMetreLines() {
      Quote quote =
          engine.quote(
              new QuoteSpec(
                  Optional.of(new DrillingSpec(SYMMETRIC, Metres.ZERO)),
                  Optional.of(new PumpSpec(HAND, Metres.ZERO)),
                  Optional.empty(),
                  Optional.empty()),
              context(ClientCategory.DOMESTIC));

      assertThat(quote.lines())
          .allSatisfy(l -> assertThat(l.unit()).isEqualTo(QuantityUnit.EACH))
          .hasSize(4);
    }

    @Test
    void feesAlwaysApplyEvenForTankOnlyJob() {
      Quote quote =
          engine.quote(
              new QuoteSpec(
                  Optional.empty(), Optional.empty(), Optional.of(TANK_1000), Optional.empty()),
              context(ClientCategory.DOMESTIC));

      assertThat(quote.lines())
          .extracting(QuoteLine::description)
          .containsExactly("Tank: 1,000 L", "Survey fee (Domestic)", "Authority fee (Domestic)");
      assertThat(quote.subtotal()).isEqualTo(ksh("32000.00"));
    }

    @Test
    void itemisedPlumbingPricesEachPipeRunByLength() {
      Plumbing itemised =
          new Plumbing.Itemised(
              List.of(
                  new PipeRun(PVC, 25, Metres.of(10)),
                  new PipeRun(HDPE, 32, Metres.of("12.5")),
                  new PipeRun(GALVANIZED, 50, Metres.of(3))),
              4);

      Quote quote =
          engine.quote(
              new QuoteSpec(
                  Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(itemised)),
              context(ClientCategory.DOMESTIC));

      List<QuoteLine> plumbing =
          quote.lines().stream().filter(l -> l.category() == LineCategory.PLUMBING).toList();
      assertThat(plumbing)
          .extracting(QuoteLine::description)
          .containsExactly("PVC pipe, 25 mm", "HDPE pipe, 32 mm", "Galvanized pipe, 50 mm");
      assertThat(plumbing)
          .extracting(QuoteLine::lineTotal)
          .containsExactly(ksh("5000.00"), ksh("10000.00"), ksh("3600.00"));
      assertThat(quote.totalFor(LineCategory.PLUMBING)).isEqualTo(ksh("18600.00"));
    }

    @Test
    void manualPlumbingIsOneLumpSumLine() {
      Quote quote =
          engine.quote(
              new QuoteSpec(
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(new Plumbing.Manual(ksh("12345.67"), "Kitchen and two taps"))),
              context(ClientCategory.DOMESTIC));

      QuoteLine line = quote.lines().getFirst();
      assertThat(line.description()).isEqualTo("Kitchen and two taps");
      assertThat(line.unit()).isEqualTo(QuantityUnit.EACH);
      assertThat(line.lineTotal()).isEqualTo(ksh("12345.67"));
    }

    @Test
    void plumbingPricerIsAnExtensionPoint() {
      PlumbingLinePricer perOutlet =
          p ->
              List.of(
                  new QuoteLine(
                      LineCategory.PLUMBING,
                      "Outlets",
                      BigDecimal.valueOf(p.outlets()),
                      QuantityUnit.EACH,
                      ksh("1000"),
                      ksh("1000").times(BigDecimal.valueOf(p.outlets()))));
      Plumbing itemised = new Plumbing.Itemised(List.of(new PipeRun(PVC, 25, Metres.of(1))), 3);

      Quote quote =
          new PricingEngine(perOutlet)
              .quote(
                  new QuoteSpec(
                      Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(itemised)),
                  context(ClientCategory.DOMESTIC));

      assertThat(quote.totalFor(LineCategory.PLUMBING)).isEqualTo(ksh("3000.00"));
    }
  }

  @Nested
  class Vat {

    @Test
    void vatIsRoundedHalfUpToCents() {
      // 0.75 × 0.14 = 0.105 → HALF_UP 0.11 (HALF_EVEN would give 0.10).
      assertThat(engine.vat(ksh("0.75"), TaxRate.of("0.14"))).isEqualTo(ksh("0.11"));
      assertThat(engine.vat(ksh("100.03"), TaxRate.of("0.16"))).isEqualTo(ksh("16.00"));
      assertThat(engine.vat(ksh("100.04"), TaxRate.of("0.16"))).isEqualTo(ksh("16.01"));
    }

    @Test
    void zeroRateMeansNoVat() {
      assertThat(engine.vat(ksh("590000"), TaxRate.of("0"))).isEqualTo(Money.ZERO);
    }
  }

  @Test
  void specWithoutServicesIsRejected() {
    assertThatThrownBy(
            () ->
                new QuoteSpec(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
        .isInstanceOf(ValidationException.class)
        .satisfies(e -> assertThat(((ValidationException) e).messageFor("services")).isPresent());
  }

  @Test
  void negativeMetresAreRejectedBeforePricing() {
    assertThatThrownBy(() -> Metres.of("-1")).isInstanceOf(ValidationException.class);
  }
}
