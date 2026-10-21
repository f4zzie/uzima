package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.job.JobStatus;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.ui.FxTest;
import com.uzima.ui.component.StatusBadge.Tone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StatusBadgeTest {

  @BeforeAll
  static void start() {
    FxTest.startToolkit();
  }

  @ParameterizedTest
  @EnumSource(JobStatus.class)
  void everyJobStatusHasALabelAndTone(JobStatus status) {
    StatusBadge badge = FxTest.onFx(() -> StatusBadge.of(status));
    assertThat(badge.getText()).isEqualTo(status.label());
    assertThat(badge.getStyleClass()).contains("badge", badge.getTone().styleClass());
    assertThat(badge.getAccessibleText()).isEqualTo("Status: " + status.label());
  }

  @Test
  void tonesMatchMeaning() {
    assertThat(StatusBadge.toneOf(JobStatus.COMPLETED)).isEqualTo(Tone.SUCCESS);
    assertThat(StatusBadge.toneOf(JobStatus.CANCELLED)).isEqualTo(Tone.DANGER);
    assertThat(StatusBadge.toneOf(JobStatus.IN_PROGRESS)).isEqualTo(Tone.ACCENT);
    assertThat(StatusBadge.toneOf(PaymentStatus.PAID)).isEqualTo(Tone.SUCCESS);
    assertThat(StatusBadge.toneOf(PaymentStatus.PARTIALLY_PAID)).isEqualTo(Tone.WARNING);
    assertThat(StatusBadge.toneOf(PaymentStatus.NOT_PAID)).isEqualTo(Tone.NEUTRAL);
  }

  @Test
  void paymentStatusUsesHumanLabels() {
    StatusBadge badge = FxTest.onFx(() -> StatusBadge.of(PaymentStatus.PARTIALLY_PAID));
    assertThat(badge.getText()).isEqualTo("Partially paid");
  }

  @Test
  void changingToneReplacesTheStyleClass() {
    StatusBadge badge = FxTest.onFx(() -> new StatusBadge("Draft", Tone.NEUTRAL));
    FxTest.onFx(() -> badge.setText("Refund due", Tone.WARNING));
    assertThat(badge.getStyleClass()).contains("badge-warning").doesNotContain("badge-neutral");
    assertThat(badge.getText()).isEqualTo("Refund due");
  }
}
