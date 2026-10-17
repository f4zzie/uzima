package com.uzima.pricing;

import com.uzima.domain.catalog.CatalogPrices;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuantityUnit;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteLine;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The only place prices are calculated. Stateless and thread-safe: catalog prices, rate tiers and
 * the tax rate arrive in a {@link PriceBasis} for each call.
 */
public final class PricingEngine {

  private final PlumbingLinePricer plumbingPricer;

  public PricingEngine() {
    this(new LengthBasedPlumbingPricer());
  }

  public PricingEngine(PlumbingLinePricer plumbingPricer) {
    this.plumbingPricer = Objects.requireNonNull(plumbingPricer, "plumbingPricer");
  }

  /**
   * Marginal per-metre cost, like tax brackets: the metres within each tier's {@code (from, to]}
   * band are charged at that tier's rate. Fractional metres are allowed; zero metres costs nothing.
   */
  public TieredCost tieredCost(Metres metres, RateTable rates) {
    List<TierSegment> segments = new ArrayList<>();
    for (RateTier tier : rates.tiers()) {
      if (metres.compareTo(tier.from()) <= 0) {
        break;
      }
      Metres upper = tier.to().map(metres::min).orElse(metres);
      Metres inTier = upper.minus(tier.from());
      segments.add(new TierSegment(tier, inTier, tier.ratePerMetre().times(inTier.value())));
    }
    return new TieredCost(segments, Money.sum(segments.stream().map(TierSegment::amount)));
  }

  /** Prices a job specification against the given live prices. */
  public Quote quote(QuoteSpec spec, PriceBasis basis) {
    List<QuoteLine> lines = new ArrayList<>();
    spec.drilling().ifPresent(d -> lines.addAll(drillingLines(d, basis.rates())));
    spec.pump().ifPresent(p -> lines.addAll(pumpLines(p, basis.rates())));
    spec.tank().ifPresent(t -> lines.add(tankLine(t)));
    spec.plumbing().ifPresent(p -> lines.addAll(plumbingLines(p)));

    String category = basis.fees().category().label();
    lines.add(
        each(LineCategory.SURVEY_FEE, "Survey fee (" + category + ")", basis.fees().surveyFee()));
    lines.add(
        each(
            LineCategory.AUTHORITY_FEE,
            "Authority fee (" + category + ")",
            basis.fees().authorityFee()));

    Money subtotal = Money.sum(lines.stream().map(QuoteLine::lineTotal));
    Money tax = vat(subtotal, basis.taxRate());
    return new Quote(lines, subtotal, basis.taxRate(), tax, subtotal.plus(tax));
  }

  /**
   * Prices an edit to a pending job against the job's own snapshot: catalog items already on the
   * job keep the price they were quoted at, and the job's fees, rate tiers and tax rate are reused.
   * Items the edit adds that were not on the job before carry whatever price {@code edited} gives
   * them, normally the current catalog price.
   *
   * @throws com.uzima.domain.job.QuoteLockedException if the job has left PENDING_SURVEY
   */
  public Job requote(Job job, QuoteSpec edited) {
    QuoteSpec spec = edited.withPricesFrom(job.spec().catalogPrices());
    return job.withQuote(spec, quote(spec, job.priceBasis()));
  }

  /**
   * Proposes moving a pending job to current prices: each catalog item at its current price, and
   * the current fees for the client's current category, rate tiers and tax rate. The job itself is
   * not changed; the caller saves {@link Job.Repricing#after()} only if the user confirms.
   *
   * @throws com.uzima.domain.job.QuoteLockedException if the job has left PENDING_SURVEY
   */
  public Job.Repricing reprice(Job job, CatalogPrices current, PriceBasis currentBasis) {
    QuoteSpec spec = job.spec().withPricesFrom(current);
    return job.repriceTo(spec, currentBasis, quote(spec, currentBasis));
  }

  /** VAT on a VAT-exclusive amount: {@code round_half_up(subtotal × rate, 2)}. */
  public Money vat(Money subtotal, TaxRate rate) {
    return subtotal.times(rate.value());
  }

  /**
   * Splits the VAT already contained in each payment of one job.
   *
   * <p>Grand totals include VAT, so the VAT portion of a payment is {@code amount × r / (1 + r)},
   * where {@code r} is the job's saved tax rate, rounded HALF_UP to cents per payment. Reversals
   * have negative amounts and so reduce collected VAT.
   *
   * <p>Per-payment rounding can drift by a cent from the invoiced VAT. To keep a fully paid job's
   * collected VAT equal to its invoiced VAT, any payment that brings cumulative net paid exactly to
   * the grand total takes the remainder: {@code invoicedVat − (VAT of all earlier payments)}.
   *
   * @param amounts payment and reversal amounts in payment-date order
   * @return the VAT portion of each amount, in the same order
   */
  public List<Money> collectedVat(
      List<Money> amounts, TaxRate rate, Money grandTotal, Money invoicedVat) {
    BigDecimal r = rate.value();
    BigDecimal onePlusR = BigDecimal.ONE.add(r);
    List<Money> result = new ArrayList<>(amounts.size());
    Money paid = Money.ZERO;
    Money vatSoFar = Money.ZERO;
    for (Money amount : amounts) {
      paid = paid.plus(amount);
      Money portion =
          paid.compareTo(grandTotal) == 0
              ? invoicedVat.minus(vatSoFar)
              : new Money(amount.amount().multiply(r).divide(onePlusR, 2, RoundingMode.HALF_UP));
      result.add(portion);
      vatSoFar = vatSoFar.plus(portion);
    }
    return List.copyOf(result);
  }

  private List<QuoteLine> drillingLines(DrillingSpec drilling, RateTable rates) {
    List<QuoteLine> lines = new ArrayList<>();
    lines.add(
        each(
            LineCategory.DRILLING,
            "Drilling down payment (" + drilling.type().name() + ")",
            drilling.type().downPayment()));
    lines.addAll(metreLines(LineCategory.DRILLING, "Drilling", drilling.depth(), rates));
    return lines;
  }

  private List<QuoteLine> pumpLines(PumpSpec pump, RateTable rates) {
    List<QuoteLine> lines = new ArrayList<>();
    lines.add(each(LineCategory.PUMP, "Pump: " + pump.type().name(), pump.type().baseCost()));
    lines.addAll(metreLines(LineCategory.PUMP, "Pump lift height", pump.liftHeight(), rates));
    return lines;
  }

  private static QuoteLine tankLine(TankType tank) {
    return each(LineCategory.TANK, "Tank: " + tank.displayName(), tank.price());
  }

  private List<QuoteLine> plumbingLines(Plumbing plumbing) {
    return switch (plumbing) {
      case Plumbing.Itemised itemised -> plumbingPricer.price(itemised);
      case Plumbing.Manual manual ->
          List.of(each(LineCategory.PLUMBING, manual.description(), manual.amount()));
    };
  }

  /** One METRE line per tier segment, e.g. "Drilling, 100–200 m band": 50 m × 1,500. */
  private List<QuoteLine> metreLines(
      LineCategory category, String label, Metres metres, RateTable rates) {
    return tieredCost(metres, rates).segments().stream()
        .map(
            s ->
                new QuoteLine(
                    category,
                    label + ", " + s.tier().label() + " band",
                    s.metres().value(),
                    QuantityUnit.METRE,
                    s.tier().ratePerMetre(),
                    s.amount()))
        .toList();
  }

  private static QuoteLine each(LineCategory category, String description, Money price) {
    return new QuoteLine(category, description, BigDecimal.ONE, QuantityUnit.EACH, price, price);
  }
}
