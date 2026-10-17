package com.uzima.domain.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MoneyTest {

  @ParameterizedTest
  @CsvSource({
    "0, 0.00",
    "1.5, 1.50",
    "0.005, 0.01",
    "0.0049, 0.00",
    "-0.005, -0.01",
    "-0.0049, 0.00",
    "1234567.125, 1234567.13"
  })
  void constructionAlwaysHasScaleTwoAndRoundsHalfUp(String input, String expected) {
    Money money = Money.of(input);
    assertThat(money.amount().scale()).isEqualTo(2);
    assertThat(money.amount()).isEqualByComparingTo(expected);
  }

  @Test
  void equalityIgnoresInputScale() {
    assertThat(Money.of("1.5")).isEqualTo(Money.of("1.50"));
    assertThat(Money.of(15)).isEqualTo(Money.of("15.000"));
    assertThat(Money.of("1.5")).hasSameHashCodeAs(Money.of("1.50"));
  }

  @Test
  void timesRoundsHalfUp() {
    assertThat(Money.of("10.00").times(new BigDecimal("0.0005"))).isEqualTo(Money.of("0.01"));
    assertThat(Money.of("10.00").times(new BigDecimal("0.00049"))).isEqualTo(Money.ZERO);
    assertThat(Money.of("590000").times(new BigDecimal("0.16"))).isEqualTo(Money.of("94400.00"));
    assertThat(Money.of("-10.00").times(new BigDecimal("0.0005"))).isEqualTo(Money.of("-0.01"));
  }

  @Test
  void dividedByRoundsHalfUp() {
    assertThat(Money.of("1.00").dividedBy(new BigDecimal("3"))).isEqualTo(Money.of("0.33"));
    assertThat(Money.of("2.00").dividedBy(new BigDecimal("3"))).isEqualTo(Money.of("0.67"));
    assertThat(Money.of("0.01").dividedBy(new BigDecimal("2"))).isEqualTo(Money.of("0.01"));
    assertThat(Money.of("-0.01").dividedBy(new BigDecimal("2"))).isEqualTo(Money.of("-0.01"));
  }

  @Test
  void arithmetic() {
    Money a = Money.of("100.25");
    Money b = Money.of("0.75");
    assertThat(a.plus(b)).isEqualTo(Money.of(101));
    assertThat(a.minus(b)).isEqualTo(Money.of("99.50"));
    assertThat(b.minus(a)).isEqualTo(Money.of("-99.50"));
    assertThat(a.negate()).isEqualTo(Money.of("-100.25"));
    assertThat(Money.sum(Stream.of(a, b, Money.of(-1)))).isEqualTo(Money.of(100));
    assertThat(Money.sum(Stream.empty())).isEqualTo(Money.ZERO);
  }

  @Test
  void comparisons() {
    Money small = Money.of(1);
    Money big = Money.of(2);
    assertThat(small.isLessThan(big)).isTrue();
    assertThat(big.isGreaterThan(small)).isTrue();
    assertThat(small.isGreaterThan(small)).isFalse();
    assertThat(small.max(big)).isEqualTo(big);
    assertThat(big.max(small)).isEqualTo(big);
    assertThat(small.min(big)).isEqualTo(small);
    assertThat(big.min(small)).isEqualTo(small);
    assertThat(big.compareTo(small)).isPositive();
    assertThat(Money.ZERO.isZero()).isTrue();
    assertThat(Money.ZERO.isPositive()).isFalse();
    assertThat(Money.ZERO.isNegative()).isFalse();
    assertThat(Money.of(-1).isNegative()).isTrue();
    assertThat(Money.of(1).isPositive()).isTrue();
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "1234567 | KSh 1,234,567.00 | 1,234,567.00",
        "0       | KSh 0.00         | 0.00",
        "-1000   | -KSh 1,000.00    | -1,000.00",
        "999.995 | KSh 1,000.00     | 1,000.00",
        "12.3    | KSh 12.30        | 12.30"
      })
  void formats(String amount, String formatted, String plain) {
    Money money = Money.of(amount);
    assertThat(money.format()).isEqualTo(formatted);
    assertThat(money.toString()).isEqualTo(formatted);
    assertThat(money.formatPlain()).isEqualTo(plain);
  }

  @Test
  void formatIsIndependentOfDefaultLocale() {
    Locale original = Locale.getDefault();
    try {
      for (Locale locale : new Locale[] {Locale.GERMANY, Locale.FRANCE, Locale.of("sw", "KE")}) {
        Locale.setDefault(locale);
        assertThat(Money.of("1234567.5").format()).isEqualTo("KSh 1,234,567.50");
        assertThat(Money.of("-1234.5").formatPlain()).isEqualTo("-1,234.50");
      }
    } finally {
      Locale.setDefault(original);
    }
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "1,234.50     | 1234.50",
        "KSh 1,234.50 | 1234.50",
        "ksh. 50      | 50.00",
        "KSH50        | 50.00",
        "0.5          | 0.50",
        "-20          | -20.00"
      })
  void parseAcceptsCommonInput(String text, String expected) {
    assertThat(Money.parse(text, "amount")).isEqualTo(Money.of(expected));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "abc", "1.234", "KSh", "12,34.5.6"})
  void parseRejectsInvalidInputWithFieldName(String text) {
    assertThatThrownBy(() -> Money.parse(text, "amount"))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> {
              assertThat(e.errors()).hasSize(1);
              assertThat(e.errors().getFirst().field()).isEqualTo("amount");
            });
  }

  @Test
  void parseRejectsNull() {
    assertThatThrownBy(() -> Money.parse(null, "paid"))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.messageFor("paid")).contains("Enter an amount"));
  }

  @Test
  void threeDecimalPlacesAreRejectedNotRounded() {
    assertThatThrownBy(() -> Money.parse("1.234", "amount"))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.messageFor("amount").orElseThrow()).contains("two decimal"));
    assertThat(Money.parse("1.230", "amount")).isEqualTo(Money.of("1.23"));
  }

  @Test
  void currencyIsKenyanShilling() {
    assertThat(Money.CURRENCY).isEqualTo("KES");
  }
}
