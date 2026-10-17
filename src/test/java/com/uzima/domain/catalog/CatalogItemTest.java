package com.uzima.domain.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.FieldError;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CatalogItemTest {

  static Stream<Arguments> namedItems() {
    return Stream.of(
        Arguments.of(
            "downPayment",
            (BiFunction<String, Money, CatalogItem>) (n, p) -> new DrillingType(1, n, p, true)),
        Arguments.of(
            "baseCost",
            (BiFunction<String, Money, CatalogItem>) (n, p) -> new PumpType(1, n, p, true)),
        Arguments.of(
            "costPerMetre",
            (BiFunction<String, Money, CatalogItem>) (n, p) -> new PipeType(1, n, p, true)));
  }

  @ParameterizedTest
  @MethodSource("namedItems")
  void namedItemsValidateAndTrim(String priceField, BiFunction<String, Money, CatalogItem> make) {
    CatalogItem item = make.apply("  Symmetric ", Money.of(130_000));
    assertThat(item.displayName()).isEqualTo("Symmetric");
    assertThat(item.price()).isEqualTo(Money.of(130_000));
    assertThat(item.active()).isTrue();
    assertThat(item.id()).isEqualTo(1);
    assertThat(make.apply("Free", Money.ZERO).price()).isEqualTo(Money.ZERO);

    assertThatThrownBy(() -> make.apply(" ", Money.of(-1)))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(FieldError::field)
                    .containsExactlyInAnyOrder("name", priceField));
    assertThatThrownBy(() -> make.apply("x".repeat(81), Money.of(1)))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("name")).isPresent());
    assertThatThrownBy(() -> make.apply("Ok", null))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor(priceField)).isPresent());
  }

  @Test
  void drillingTypePriceChangeAndRetire() {
    DrillingType core = new DrillingType(2, "Core", Money.of(225_000), true);
    DrillingType repriced = core.withPrice(Money.of(230_000));
    assertThat(repriced.downPayment()).isEqualTo(Money.of(230_000));
    assertThat(repriced.name()).isEqualTo("Core");
    assertThat(core.downPayment()).isEqualTo(Money.of(225_000));
    assertThat(core.retired().active()).isFalse();
    assertThat(core.retired().downPayment()).isEqualTo(core.downPayment());
  }

  @Test
  void pumpTypePriceChangeAndRetire() {
    PumpType solar = new PumpType(3, "Solar", Money.of(65_000), true);
    assertThat(solar.withPrice(Money.of(70_000)).baseCost()).isEqualTo(Money.of(70_000));
    assertThat(solar.retired().active()).isFalse();
    assertThat(solar.retired().name()).isEqualTo("Solar");
  }

  @Test
  void pipeTypePriceChangeAndRetire() {
    PipeType hdpe = new PipeType(4, "HDPE", Money.of(800), true);
    assertThat(hdpe.withPrice(Money.of(850)).costPerMetre()).isEqualTo(Money.of(850));
    assertThat(hdpe.retired().active()).isFalse();
  }

  @Test
  void tankTypeDisplayAndValidation() {
    TankType tank = new TankType(5, 5000, Money.of(65_000), true);
    assertThat(tank.displayName()).isEqualTo("5,000 L");
    assertThat(new TankType(6, 10_000, Money.of(125_000), true).displayName())
        .isEqualTo("10,000 L");
    assertThat(new TankType(7, 500, Money.of(9_000), true).displayName()).isEqualTo("500 L");
    assertThat(tank.withPrice(Money.of(60_000)).price()).isEqualTo(Money.of(60_000));
    assertThat(tank.retired().active()).isFalse();
    assertThat(tank.retired().capacityLitres()).isEqualTo(5000);

    assertThatThrownBy(() -> new TankType(1, 0, Money.of(1), true))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("capacityLitres")).isPresent());
    assertThatThrownBy(() -> new TankType(1, -5, Money.of(1), true))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(() -> new TankType(1, 1000, Money.of(-1), true))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("price")).isPresent());
  }

  @Test
  void catalogItemIsSealedOverTheFourTypes() {
    List<CatalogItem> items =
        List.of(
            new DrillingType(1, "Symmetric", Money.of(130_000), true),
            new PumpType(1, "Hand", Money.of(30_000), true),
            new TankType(1, 1000, Money.of(15_000), true),
            new PipeType(1, "PVC", Money.of(500), true));
    List<String> kinds =
        items.stream()
            .map(
                item ->
                    switch (item) {
                      case DrillingType d -> "drilling";
                      case PumpType p -> "pump";
                      case TankType t -> "tank";
                      case PipeType p -> "pipe";
                    })
            .toList();
    assertThat(kinds).containsExactly("drilling", "pump", "tank", "pipe");
  }

  @Test
  void categoryFees() {
    CategoryFees fees =
        new CategoryFees(ClientCategory.INDUSTRIAL, Money.of(20_000), Money.of(50_000));
    assertThat(fees.surveyFee()).isEqualTo(Money.of(20_000));
    assertThat(fees.authorityFee()).isEqualTo(Money.of(50_000));
    assertThat(new CategoryFees(ClientCategory.DOMESTIC, Money.ZERO, Money.ZERO).surveyFee())
        .isEqualTo(Money.ZERO);

    assertThatThrownBy(() -> new CategoryFees(ClientCategory.DOMESTIC, Money.of(-1), Money.of(-1)))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(FieldError::field)
                    .containsExactly("surveyFee", "authorityFee"));
    assertThatThrownBy(() -> new CategoryFees(ClientCategory.DOMESTIC, null, Money.ZERO))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(() -> new CategoryFees(null, Money.ZERO, Money.ZERO))
        .isInstanceOf(NullPointerException.class);
  }
}
