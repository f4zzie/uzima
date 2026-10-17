package com.uzima.domain.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TaxRateTest {

  @Test
  void kenyanStandardRateIsSixteenPercent() {
    assertThat(TaxRate.KENYA_STANDARD.value()).isEqualByComparingTo("0.16");
    assertThat(TaxRate.KENYA_STANDARD.formatPercent()).isEqualTo("16%");
    assertThat(TaxRate.KENYA_STANDARD.toString()).isEqualTo("16%");
  }

  @Test
  void valueIsNormalisedToScaleFour() {
    assertThat(TaxRate.of("0.16").value().scale()).isEqualTo(4);
    assertThat(TaxRate.of("0.16")).isEqualTo(TaxRate.of("0.1600"));
  }

  @ParameterizedTest
  @CsvSource({"16, 0.16, 16%", "14.5%, 0.145, 14.5%", "' 0 ', 0, 0%", "99.99, 0.9999, 99.99%"})
  void parsesPercentages(String text, String fraction, String formatted) {
    TaxRate rate = TaxRate.parsePercent(text);
    assertThat(rate.value()).isEqualByComparingTo(fraction);
    assertThat(rate.formatPercent()).isEqualTo(formatted);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "1", "100", "1.5", "0.16001"})
  void rejectsOutOfRangeOrTooPreciseFractions(String fraction) {
    assertThatThrownBy(() -> new TaxRate(new BigDecimal(fraction)))
        .isInstanceOf(ValidationException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "", "100", "-5", "16.001"})
  void parsePercentRejectsGarbageAndOutOfRange(String text) {
    assertThatThrownBy(() -> TaxRate.parsePercent(text))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors().getFirst().field()).isEqualTo("taxRate"));
  }

  @Test
  void parsePercentRejectsNull() {
    assertThatThrownBy(() -> TaxRate.parsePercent(null)).isInstanceOf(ValidationException.class);
  }
}
