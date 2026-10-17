package com.uzima.domain.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class JobNumberTest {

  @Test
  void formatsWithFiveDigitSequence() {
    assertThat(new JobNumber(Year.of(2026), 42).value()).isEqualTo("JOB-2026-00042");
    assertThat(new JobNumber(Year.of(2026), 42)).hasToString("JOB-2026-00042");
  }

  @Test
  void widensPastFiveDigits() {
    JobNumber big = new JobNumber(Year.of(2026), 123456);
    assertThat(big.value()).isEqualTo("JOB-2026-123456");
    assertThat(JobNumber.parse(big.value())).isEqualTo(big);
  }

  @Test
  void parseRoundTrips() {
    JobNumber n = JobNumber.parse(" JOB-2025-00007 ");
    assertThat(n).isEqualTo(new JobNumber(Year.of(2025), 7));
    assertThat(JobNumber.parse(n.value())).isEqualTo(n);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(
      strings = {
        "",
        "JOB-2026-42",
        "job-2026-00042",
        "RCP-2026-00042",
        "JOB-26-00042",
        "JOB-2026-00000"
      })
  void rejectsMalformed(String text) {
    assertThatThrownBy(() -> JobNumber.parse(text))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("jobNumber")).isPresent());
  }

  @Test
  void sequenceMustBePositive() {
    assertThatThrownBy(() -> new JobNumber(Year.of(2026), 0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
