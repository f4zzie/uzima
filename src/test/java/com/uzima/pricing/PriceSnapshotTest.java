package com.uzima.pricing;

import static com.uzima.pricing.PricingFixtures.SYMMETRIC;
import static com.uzima.pricing.PricingFixtures.context;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.time.Instant;
import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Acceptance example 5 and section 5.9: saved jobs keep the prices and tax rate they were quoted
 * at.
 */
class PriceSnapshotTest {

  private final PricingEngine engine = new PricingEngine();

  private static QuoteSpec drilling(DrillingType type, String metres) {
    return new QuoteSpec(
        Optional.of(new DrillingSpec(type, Metres.of(metres))),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  private Job savedJob() {
    QuoteSpec spec = drilling(SYMMETRIC, "80");
    return Job.create(
        new JobNumber(Year.of(2026), 1),
        1,
        spec,
        engine.quote(spec, context(ClientCategory.DOMESTIC)),
        context(ClientCategory.DOMESTIC),
        "",
        Instant.EPOCH,
        1);
  }

  @Test
  void changingTaxRateAffectsNewQuotesButNotSavedJobs() {
    Job saved = savedJob();

    QuoteSpec spec = drilling(SYMMETRIC, "80");
    var newQuote = engine.quote(spec, context(ClientCategory.DOMESTIC, TaxRate.of("0.14")));

    // 210,000 drilling + 7,000 survey + 10,000 authority = 227,000
    assertThat(newQuote.taxRate()).isEqualTo(TaxRate.of("0.14"));
    assertThat(newQuote.tax()).isEqualTo(Money.of("31780.00"));
    assertThat(saved.quote().taxRate()).isEqualTo(TaxRate.of("0.16"));
    assertThat(saved.quote().tax()).isEqualTo(Money.of("36320.00"));
    assertThat(saved.quote().grandTotal()).isEqualTo(Money.of("263320.00"));
  }

  @Test
  void changingCatalogPriceAffectsNewQuotesButNotSavedJobs() {
    Job saved = savedJob();

    DrillingType repriced = SYMMETRIC.withPrice(Money.of(150_000));
    var newQuote = engine.quote(drilling(repriced, "80"), context(ClientCategory.DOMESTIC));

    assertThat(newQuote.subtotal()).isEqualTo(Money.of(247_000));
    assertThat(saved.quote().subtotal()).isEqualTo(Money.of(227_000));
    assertThat(saved.quote().lines().getFirst().unitPrice()).isEqualTo(Money.of(130_000));
  }
}
