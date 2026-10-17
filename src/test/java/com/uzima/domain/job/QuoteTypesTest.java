package com.uzima.domain.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuoteTypesTest {

  private static final PipeType PVC = new PipeType(1, "PVC", Money.of(500), true);

  private static QuoteLine line(String total) {
    Money amount = Money.of(total);
    return new QuoteLine(
        LineCategory.TANK, "Tank", BigDecimal.ONE, QuantityUnit.EACH, amount, amount);
  }

  @Test
  void quoteRejectsSubtotalThatDoesNotMatchLines() {
    assertThatThrownBy(
            () ->
                new Quote(
                    List.of(line("100.00")),
                    Money.of("99.00"),
                    TaxRate.of("0.16"),
                    Money.of("15.84"),
                    Money.of("114.84")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("sum of lines");
  }

  @Test
  void quoteRejectsGrandTotalThatIsNotSubtotalPlusTax() {
    assertThatThrownBy(
            () ->
                new Quote(
                    List.of(line("100.00")),
                    Money.of("100.00"),
                    TaxRate.of("0.16"),
                    Money.of("16.00"),
                    Money.of("117.00")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("subtotal + tax");
  }

  @Test
  void quoteLinesAreAnImmutableCopy() {
    List<QuoteLine> lines = new java.util.ArrayList<>(List.of(line("100.00")));
    Quote quote =
        new Quote(
            lines, Money.of("100.00"), TaxRate.of("0.16"), Money.of("16.00"), Money.of("116.00"));
    lines.add(line("1.00"));

    assertThat(quote.lines()).hasSize(1);
    assertThatThrownBy(() -> quote.lines().add(line("1.00")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void quoteLineRequiresPositiveQuantity() {
    assertThatThrownBy(
            () ->
                new QuoteLine(
                    LineCategory.DRILLING,
                    "Drilling",
                    BigDecimal.ZERO,
                    QuantityUnit.METRE,
                    Money.of(1_000),
                    Money.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void pipeRunValidatesAllFieldsAtOnce() {
    assertThatThrownBy(() -> new PipeRun(null, 0, Metres.ZERO))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(f -> f.field())
                    .containsExactly("pipe", "diameterMm", "length"));
  }

  @Test
  void itemisedPlumbingNeedsAtLeastOneRunAndNonNegativeOutlets() {
    assertThatThrownBy(() -> new Plumbing.Itemised(List.of(), -1))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(f -> f.field())
                    .containsExactly("plumbingRuns", "outlets"));
    assertThat(new Plumbing.Itemised(List.of(new PipeRun(PVC, 25, Metres.of(5))), 0).outlets())
        .isZero();
  }

  @Test
  void manualPlumbingNeedsPositiveAmountAndDescription() {
    assertThatThrownBy(() -> new Plumbing.Manual(Money.ZERO, " "))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(f -> f.field())
                    .containsExactly("plumbingAmount", "plumbingDescription"));
    assertThat(new Plumbing.Manual(Money.of(10), "  Taps  ").description()).isEqualTo("Taps");
  }

  @Test
  void lineCategoryHasLabels() {
    assertThat(LineCategory.SURVEY_FEE.label()).isEqualTo("Survey fee");
    assertThat(QuantityUnit.METRE.symbol()).isEqualTo("m");
  }
}
