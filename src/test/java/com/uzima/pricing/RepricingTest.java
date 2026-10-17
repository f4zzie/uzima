package com.uzima.pricing;

import static com.uzima.pricing.PricingFixtures.PVC;
import static com.uzima.pricing.PricingFixtures.SYMMETRIC;
import static com.uzima.pricing.PricingFixtures.TANK_5000;
import static com.uzima.pricing.PricingFixtures.context;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.CatalogPrices;
import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.PipeRun;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.QuoteLockedException;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Section 5.9 for jobs still awaiting survey: edits keep the snapshot, and moving to current prices
 * happens only through an explicit reprice that shows before and after.
 */
class RepricingTest {

  private final PricingEngine engine = new PricingEngine();

  // After the job is saved, the catalog and Settings change:
  private static final DrillingType SYMMETRIC_NOW = SYMMETRIC.withPrice(Money.of(150_000));
  private static final TankType TANK_5000_NOW = TANK_5000.withPrice(Money.of(70_000));
  private static final PipeType PVC_NOW = PVC.withPrice(Money.of(600));
  private static final RateTable RATES_NOW =
      new RateTable(
          List.of(
              RateTier.bounded(0, 100, 1_200),
              RateTier.bounded(100, 200, 1_500),
              RateTier.bounded(200, 300, 2_000),
              RateTier.openEnded(300, 2_500)));
  private static final PriceBasis DOMESTIC_NOW =
      new PriceBasis(
          new CategoryFees(ClientCategory.DOMESTIC, Money.of(8_000), Money.of(12_000)),
          RATES_NOW,
          TaxRate.of("0.14"));
  private static final CatalogPrices CATALOG_NOW = catalog(SYMMETRIC_NOW, TANK_5000_NOW, PVC_NOW);

  /** Domestic, Symmetric at 80 m: 210,000 + 17,000 fees = 227,000; VAT 36,320; 263,320. */
  private Job savedJob() {
    return savedJob(drilling(SYMMETRIC, "80", Optional.empty(), Optional.empty()));
  }

  private Job savedJob(QuoteSpec spec) {
    PriceBasis basis = context(ClientCategory.DOMESTIC);
    return Job.create(
        new JobNumber(Year.of(2026), 1),
        1,
        spec,
        engine.quote(spec, basis),
        basis,
        "",
        Instant.EPOCH,
        1);
  }

  private static QuoteSpec drilling(
      DrillingType type, String metres, Optional<TankType> tank, Optional<Plumbing> plumbing) {
    return new QuoteSpec(
        Optional.of(new DrillingSpec(type, Metres.of(metres))), Optional.empty(), tank, plumbing);
  }

  private static CatalogPrices catalog(CatalogItem... items) {
    return new CatalogPrices() {
      private <T extends CatalogItem> Optional<T> find(Class<T> type, long id) {
        return List.of(items).stream()
            .filter(type::isInstance)
            .map(type::cast)
            .filter(i -> i.id() == id)
            .findFirst();
      }

      @Override
      public Optional<DrillingType> drillingType(long id) {
        return find(DrillingType.class, id);
      }

      @Override
      public Optional<PumpType> pumpType(long id) {
        return find(PumpType.class, id);
      }

      @Override
      public Optional<TankType> tankType(long id) {
        return find(TankType.class, id);
      }

      @Override
      public Optional<PipeType> pipeType(long id) {
        return find(PipeType.class, id);
      }
    };
  }

  // --- Editing keeps the snapshot ---

  @Test
  void editKeepsSnapshottedDownPaymentTiersFeesAndTaxRate() {
    Job saved = savedJob();

    // The editor hands back the type from the live catalog at its new price; the snapshot wins.
    Job edited =
        engine.requote(saved, drilling(SYMMETRIC_NOW, "120", Optional.empty(), Optional.empty()));

    // 130,000 + 100 m × 1,000 + 20 m × 1,500 = 260,000; fees 7,000 + 10,000 at 16%.
    assertThat(edited.quote().totalFor(LineCategory.DRILLING)).isEqualTo(Money.of(260_000));
    assertThat(edited.quote().totalFor(LineCategory.SURVEY_FEE)).isEqualTo(Money.of(7_000));
    assertThat(edited.quote().subtotal()).isEqualTo(Money.of(277_000));
    assertThat(edited.quote().tax()).isEqualTo(Money.of(44_320));
    assertThat(edited.quote().grandTotal()).isEqualTo(Money.of(321_320));
    assertThat(edited.priceBasis()).isEqualTo(saved.priceBasis());
    assertThat(edited.spec().drilling().orElseThrow().type().downPayment())
        .isEqualTo(Money.of(130_000));
  }

  @Test
  void itemAddedDuringEditUsesThePriceItWasPickedAt() {
    Job edited =
        engine.requote(
            savedJob(),
            drilling(SYMMETRIC_NOW, "80", Optional.of(TANK_5000_NOW), Optional.empty()));

    assertThat(edited.quote().totalFor(LineCategory.TANK)).isEqualTo(Money.of(70_000));
    assertThat(edited.quote().totalFor(LineCategory.DRILLING)).isEqualTo(Money.of(210_000));
    assertThat(edited.quote().subtotal()).isEqualTo(Money.of(297_000));
  }

  @Test
  void editKeepsSnapshottedPipePrices() {
    Plumbing plumbing = new Plumbing.Itemised(List.of(new PipeRun(PVC, 25, Metres.of(10))), 2);
    Job saved = savedJob(drilling(SYMMETRIC, "80", Optional.empty(), Optional.of(plumbing)));
    Plumbing editedPlumbing =
        new Plumbing.Itemised(List.of(new PipeRun(PVC_NOW, 32, Metres.of(20))), 3);

    Job edited =
        engine.requote(
            saved, drilling(SYMMETRIC_NOW, "80", Optional.empty(), Optional.of(editedPlumbing)));

    assertThat(edited.quote().totalFor(LineCategory.PLUMBING)).isEqualTo(Money.of(10_000));
  }

  @Test
  void editIsRejectedOnceApproved() {
    Job approved = savedJob().advanceTo(JobStatus.APPROVED, Instant.EPOCH, 1).job();
    assertThatThrownBy(
            () ->
                engine.requote(
                    approved, drilling(SYMMETRIC, "90", Optional.empty(), Optional.empty())))
        .isInstanceOf(QuoteLockedException.class);
  }

  // --- Repricing only on request ---

  @Test
  void repriceShowsBeforeAndAfterAtCurrentPrices() {
    Job saved = savedJob();

    Job.Repricing r = engine.reprice(saved, CATALOG_NOW, DOMESTIC_NOW);

    // 150,000 + 80 m × 1,200 = 246,000; fees 8,000 + 12,000 → 266,000; VAT 14% 37,240.
    assertThat(r.beforeQuote().grandTotal()).isEqualTo(Money.of(263_320));
    assertThat(r.afterQuote().subtotal()).isEqualTo(Money.of(266_000));
    assertThat(r.afterQuote().tax()).isEqualTo(Money.of(37_240));
    assertThat(r.afterQuote().grandTotal()).isEqualTo(Money.of(303_240));
    assertThat(r.grandTotalChange()).isEqualTo(Money.of(39_920));
    assertThat(r.changesAnything()).isTrue();
    assertThat(r.after().priceBasis()).isEqualTo(DOMESTIC_NOW);
  }

  @Test
  void repriceLeavesTheJobUnchangedUntilAfterIsSaved() {
    Job saved = savedJob();

    Job.Repricing r = engine.reprice(saved, CATALOG_NOW, DOMESTIC_NOW);

    assertThat(r.before()).isSameAs(saved);
    assertThat(saved.quote().grandTotal()).isEqualTo(Money.of(263_320));
    assertThat(saved.priceBasis()).isEqualTo(context(ClientCategory.DOMESTIC));
    assertThat(r.after().number()).isEqualTo(saved.number());
    assertThat(r.after().status()).isEqualTo(JobStatus.PENDING_SURVEY);
  }

  @Test
  void repriceAlsoUpdatesPipePrices() {
    Plumbing plumbing = new Plumbing.Itemised(List.of(new PipeRun(PVC, 25, Metres.of(10))), 2);
    Job saved = savedJob(drilling(SYMMETRIC, "80", Optional.empty(), Optional.of(plumbing)));

    Job.Repricing r = engine.reprice(saved, CATALOG_NOW, DOMESTIC_NOW);

    assertThat(r.beforeQuote().totalFor(LineCategory.PLUMBING)).isEqualTo(Money.of(5_000));
    assertThat(r.afterQuote().totalFor(LineCategory.PLUMBING)).isEqualTo(Money.of(6_000));
  }

  @Test
  void repriceWithUnchangedCatalogChangesNothing() {
    Job saved = savedJob();

    Job.Repricing r = engine.reprice(saved, catalog(SYMMETRIC), context(ClientCategory.DOMESTIC));

    assertThat(r.changesAnything()).isFalse();
    assertThat(r.grandTotalChange()).isEqualTo(Money.ZERO);
  }

  @Test
  void itemsMissingFromCurrentCatalogKeepTheirSnapshotPrice() {
    Job.Repricing r = engine.reprice(savedJob(), catalog(), context(ClientCategory.DOMESTIC));

    assertThat(r.afterQuote().totalFor(LineCategory.DRILLING)).isEqualTo(Money.of(210_000));
  }

  @Test
  void clientCategoryChangeReachesAPendingJobOnlyThroughReprice() {
    Job saved = savedJob();

    // Client moved from Domestic to Commercial. A plain edit still uses Domestic fees.
    Job edited =
        engine.requote(saved, drilling(SYMMETRIC, "80", Optional.empty(), Optional.empty()));
    assertThat(edited.quote().grandTotal()).isEqualTo(Money.of(263_320));

    // Reprice with the client's current category: 210,000 + 15,000 + 30,000 = 255,000.
    Job.Repricing r = engine.reprice(saved, catalog(SYMMETRIC), context(ClientCategory.COMMERCIAL));
    assertThat(r.afterQuote().totalFor(LineCategory.SURVEY_FEE)).isEqualTo(Money.of(15_000));
    assertThat(r.afterQuote().totalFor(LineCategory.AUTHORITY_FEE)).isEqualTo(Money.of(30_000));
    assertThat(r.afterQuote().grandTotal()).isEqualTo(Money.of(295_800));
    assertThat(r.grandTotalChange()).isEqualTo(Money.of(32_480));
  }

  @Test
  void repriceIsRejectedOnceApproved() {
    Job approved = savedJob().advanceTo(JobStatus.APPROVED, Instant.EPOCH, 1).job();
    assertThatThrownBy(() -> engine.reprice(approved, CATALOG_NOW, DOMESTIC_NOW))
        .isInstanceOf(QuoteLockedException.class);
  }
}
