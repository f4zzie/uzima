package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.money.Money;
import com.uzima.domain.payment.PaymentStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The snapshot story holds together: numbers, totals and balances match what the docs say. */
class SampleDataTest {

  private static SampleData data;

  @BeforeAll
  static void seed() {
    data = SampleData.seed();
  }

  @AfterAll
  static void close() {
    data.close();
  }

  @Test
  void numbersAreIssuedInStoryOrder() {
    assertThat(data.athiRiver.code().value()).isEqualTo("UZM-IND-0001");
    assertThat(data.wanjiru.code().value()).isEqualTo("UZM-DOM-0001");
    assertThat(data.stMarys.code().value()).isEqualTo("UZM-COM-0002");
    assertThat(data.jobs().stream().map(j -> j.number().value()))
        .containsExactly(
            "JOB-2026-00001",
            "JOB-2026-00002",
            "JOB-2026-00003",
            "JOB-2026-00004",
            "JOB-2026-00005",
            "JOB-2026-00006");
    assertThat(data.athiRiverDeposit.receiptNumber().value()).isEqualTo("RCP-2026-00001");
  }

  @Test
  void balancesMatchTheStory() {
    var athi = data.services.payments().account(data.athiRiverJob.id());
    assertThat(data.athiRiverJob.quote().grandTotal()).isEqualTo(Money.of(684_400));
    assertThat(athi.balance()).isEqualTo(Money.of(600_000));
    assertThat(data.services.payments().account(data.wanjiruJob.id()).status())
        .isEqualTo(PaymentStatus.PAID);
    assertThat(data.services.payments().account(data.stMarysJob.id()).refundDue())
        .isEqualTo(Money.of(200_000));
    assertThat(data.services.clock().instant().atZone(data.clock.getZone()).toLocalDate())
        .isEqualTo(SampleData.TODAY);
  }
}
