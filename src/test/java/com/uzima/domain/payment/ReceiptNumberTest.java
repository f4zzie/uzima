package com.uzima.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ReceiptNumberTest {

  @Test
  void formatsAndParses() {
    ReceiptNumber n = new ReceiptNumber(Year.of(2026), 42);
    assertThat(n.value()).isEqualTo("RCP-2026-00042");
    assertThat(n).hasToString("RCP-2026-00042");
    assertThat(ReceiptNumber.parse(" RCP-2026-00042 ")).isEqualTo(n);
  }

  @Test
  void widensPastFiveDigits() {
    ReceiptNumber big = new ReceiptNumber(Year.of(2027), 100000);
    assertThat(big.value()).isEqualTo("RCP-2027-100000");
    assertThat(ReceiptNumber.parse(big.value())).isEqualTo(big);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "RCP-2026-1", "JOB-2026-00042", "rcp-2026-00042", "RCP-2026-00000"})
  void rejectsMalformed(String text) {
    assertThatThrownBy(() -> ReceiptNumber.parse(text))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("receiptNumber")).isPresent());
  }

  @Test
  void sequenceMustBePositive() {
    assertThatThrownBy(() -> new ReceiptNumber(Year.of(2026), -1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
