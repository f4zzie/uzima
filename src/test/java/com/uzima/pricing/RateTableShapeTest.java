package com.uzima.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The tier shapes the pricing engine relies on are enforced whenever a table is built. */
class RateTableShapeTest {

  @Test
  void acceptsContiguousTiersEndingOpen() {
    assertThat(PricingFixtures.STANDARD_RATES.tiers()).hasSize(4);
  }

  @Test
  void rejectsEmptyTable() {
    assertRejected(List.of(), "at least one");
  }

  @Test
  void rejectsFirstTierNotStartingAtZero() {
    assertRejected(
        List.of(RateTier.bounded(10, 100, 1_000), RateTier.openEnded(100, 1_500)), "0 m");
  }

  @Test
  void rejectsGapBetweenTiers() {
    assertRejected(
        List.of(RateTier.bounded(0, 100, 1_000), RateTier.openEnded(120, 1_500)), "must start at");
  }

  @Test
  void rejectsOverlappingTiers() {
    assertRejected(
        List.of(RateTier.bounded(0, 100, 1_000), RateTier.openEnded(90, 1_500)), "must start at");
  }

  @Test
  void rejectsOpenEndedTierInTheMiddle() {
    assertRejected(
        List.of(RateTier.openEnded(0, 1_000), RateTier.openEnded(100, 1_500)), "Only the last");
  }

  @Test
  void rejectsBoundedLastTier() {
    assertRejected(
        List.of(RateTier.bounded(0, 100, 1_000), RateTier.bounded(100, 200, 1_500)),
        "last tier must have no upper limit");
  }

  @Test
  void rejectsTierThatEndsBeforeItStarts() {
    assertThatThrownBy(
            () -> new RateTier(Metres.of(100), Optional.of(Metres.of(100)), Money.of(1_000)))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void rejectsZeroRate() {
    assertThatThrownBy(() -> RateTier.openEnded(0, 0)).isInstanceOf(ValidationException.class);
  }

  private static void assertRejected(List<RateTier> tiers, String messagePart) {
    assertThatThrownBy(() -> new RateTable(tiers))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining(messagePart);
  }
}
