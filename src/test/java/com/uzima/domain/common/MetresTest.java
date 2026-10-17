package com.uzima.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MetresTest {

  @ParameterizedTest
  @CsvSource({
    "150, 150.00, 150 m",
    "100.5, 100.50, 100.5 m",
    "0, 0.00, 0 m",
    "300.01, 300.01, 300.01 m"
  })
  void normalisesToScaleTwo(String input, String value, String display) {
    Metres m = Metres.of(input);
    assertThat(m.value().toPlainString()).isEqualTo(value);
    assertThat(m.toString()).isEqualTo(display);
  }

  @Test
  void ofLong() {
    assertThat(Metres.of(150)).isEqualTo(Metres.of("150.0"));
    assertThat(Metres.of(0).isZero()).isTrue();
    assertThat(Metres.ZERO.isZero()).isTrue();
    assertThat(Metres.of(1).isZero()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "-0.01", "1.234", "1000000", "abc", ""})
  void rejectsInvalidValues(String input) {
    assertThatThrownBy(() -> Metres.of(input))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors().getFirst().field()).isEqualTo("metres"));
  }

  @Test
  void acceptsMaximumAndTrailingZeros() {
    assertThat(Metres.of("999999.99").value()).isEqualByComparingTo("999999.99");
    assertThat(Metres.of("1.230")).isEqualTo(Metres.of("1.23"));
  }

  @Test
  void rejectsNull() {
    assertThatThrownBy(() -> new Metres((BigDecimal) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void minMinusAndCompare() {
    Metres a = Metres.of("150");
    Metres b = Metres.of("100.5");
    assertThat(a.min(b)).isEqualTo(b);
    assertThat(b.min(a)).isEqualTo(b);
    assertThat(a.minus(b)).isEqualTo(Metres.of("49.5"));
    assertThat(a.compareTo(b)).isPositive();
    assertThat(b.compareTo(a)).isNegative();
    assertThat(a.compareTo(Metres.of("150.00"))).isZero();
    assertThatThrownBy(() -> b.minus(a)).isInstanceOf(ValidationException.class);
  }
}
