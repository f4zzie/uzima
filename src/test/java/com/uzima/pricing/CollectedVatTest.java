package com.uzima.pricing;

import static com.uzima.pricing.PricingFixtures.VAT_16;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.money.Money;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CollectedVatTest {

  private final PricingEngine engine = new PricingEngine();

  private static List<Money> amounts(String... values) {
    return Stream.of(values).map(Money::of).toList();
  }

  private static Money sum(List<Money> values) {
    return Money.sum(values.stream());
  }

  @Test
  void singleFullPaymentCarriesTheInvoicedVat() {
    // Example 1: total 684,400.00 of which VAT 94,400.00.
    List<Money> vat =
        engine.collectedVat(
            amounts("684400.00"), VAT_16, Money.of("684400.00"), Money.of("94400.00"));

    assertThat(vat).containsExactly(Money.of("94400.00"));
  }

  @Test
  void partialPaymentsUseAmountTimesRateOverOnePlusRate() {
    List<Money> vat =
        engine.collectedVat(
            amounts("100000.00", "50000.00"), VAT_16, Money.of("684400.00"), Money.of("94400.00"));

    // 100,000 × 0.16 / 1.16 = 13,793.1034…; 50,000 × 0.16 / 1.16 = 6,896.5517…
    assertThat(vat).containsExactly(Money.of("13793.10"), Money.of("6896.55"));
  }

  @Test
  void reversalsReduceCollectedVat() {
    List<Money> vat =
        engine.collectedVat(
            amounts("100000.00", "50000.00", "-50000.00"),
            VAT_16,
            Money.of("684400.00"),
            Money.of("94400.00"));

    assertThat(vat)
        .containsExactly(Money.of("13793.10"), Money.of("6896.55"), Money.of("-6896.55"));
    assertThat(sum(vat)).isEqualTo(Money.of("13793.10"));
  }

  /**
   * Example 1's total paid in three instalments of 228,133.33 + 228,133.33 + 228,133.34. Rounded
   * per payment, each carries 31,466.67 of VAT, which sums to 94,400.01: one cent more than the
   * invoiced 94,400.00. The final payment takes the remainder, 31,466.66.
   */
  @Test
  void remainderGoesOnFinalPaymentWhenRoundingWouldDriftByACent() {
    List<Money> vat =
        engine.collectedVat(
            amounts("228133.33", "228133.33", "228133.34"),
            VAT_16,
            Money.of("684400.00"),
            Money.of("94400.00"));

    assertThat(vat)
        .containsExactly(Money.of("31466.67"), Money.of("31466.67"), Money.of("31466.66"));
    assertThat(sum(vat)).isEqualTo(Money.of("94400.00"));
  }

  /** Small amounts: 38.67 + 38.67 + 38.66 on a 116.00 job would naively collect 15.99 of 16.00. */
  @Test
  void remainderRuleAlsoCorrectsDriftDownwards() {
    List<Money> vat =
        engine.collectedVat(
            amounts("38.67", "38.67", "38.66"), VAT_16, Money.of("116.00"), Money.of("16.00"));

    assertThat(vat).containsExactly(Money.of("5.33"), Money.of("5.33"), Money.of("5.34"));
    assertThat(sum(vat)).isEqualTo(Money.of("16.00"));
  }

  @Test
  void fullyPaidJobCollectsExactlyItsInvoicedVat() {
    // Example 2 (298,120.00 incl. 41,120.00 VAT) paid in four uneven instalments.
    List<Money> vat =
        engine.collectedVat(
            amounts("100000.01", "99999.99", "48120.37", "49999.63"),
            VAT_16,
            Money.of("298120.00"),
            Money.of("41120.00"));

    assertThat(sum(vat)).isEqualTo(Money.of("41120.00"));
  }

  @Test
  void reversedFullPaymentThenRepaidStillMatchesInvoicedVat() {
    List<Money> vat =
        engine.collectedVat(
            amounts("116.00", "-116.00", "116.00"), VAT_16, Money.of("116.00"), Money.of("16.00"));

    assertThat(vat).containsExactly(Money.of("16.00"), Money.of("-16.00"), Money.of("16.00"));
    assertThat(sum(vat)).isEqualTo(Money.of("16.00"));
  }

  @Test
  void noPaymentsMeansNoVat() {
    assertThat(engine.collectedVat(List.of(), VAT_16, Money.of("116.00"), Money.of("16.00")))
        .isEmpty();
  }
}
