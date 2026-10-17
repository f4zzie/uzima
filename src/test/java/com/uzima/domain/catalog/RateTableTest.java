package com.uzima.domain.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RateTableTest {

  static List<RateTier> standardTiers() {
    return List.of(
        RateTier.bounded(0, 100, 1_000),
        RateTier.bounded(100, 200, 1_500),
        RateTier.bounded(200, 300, 2_000),
        RateTier.openEnded(300, 2_500));
  }

  @Test
  void standardFourTierTableIsValid() {
    RateTable table = new RateTable(standardTiers());
    assertThat(table.tiers()).hasSize(4);
    assertThat(table.tiers().getLast().isOpenEnded()).isTrue();
    assertThat(table.tiers().getFirst().isOpenEnded()).isFalse();
  }

  @Test
  void singleOpenEndedTierIsValid() {
    assertThat(new RateTable(List.of(RateTier.openEnded(0, 900))).tiers()).hasSize(1);
  }

  @Test
  void fractionalBoundariesAreAllowedWhenContiguous() {
    List<RateTier> tiers =
        List.of(
            new RateTier(Metres.ZERO, Optional.of(Metres.of("50.5")), Money.of(700)),
            new RateTier(Metres.of("50.5"), Optional.empty(), Money.of(900)));
    assertThat(new RateTable(tiers).tiers()).hasSize(2);
  }

  @Test
  void tiersAreCopiedDefensively() {
    List<RateTier> source = new ArrayList<>(standardTiers());
    RateTable table = new RateTable(source);
    source.clear();
    assertThat(table.tiers()).hasSize(4);
    assertThatThrownBy(() -> table.tiers().add(RateTier.openEnded(0, 1)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void emptyTableRejected() {
    assertRejected(List.of(), "at least one");
  }

  @Test
  void firstTierMustStartAtZero() {
    assertRejected(
        List.of(RateTier.bounded(10, 100, 1_000), RateTier.openEnded(100, 1_500)), "start at 0");
  }

  @Test
  void gapRejected() {
    assertRejected(
        List.of(RateTier.bounded(0, 100, 1_000), RateTier.openEnded(120, 1_500)),
        "Tier 2 must start at 100 m");
  }

  @Test
  void overlapRejected() {
    assertRejected(
        List.of(
            RateTier.bounded(0, 100, 1_000),
            RateTier.bounded(90, 200, 1_500),
            RateTier.openEnded(200, 2_000)),
        "Tier 2 must start at 100 m");
  }

  @Test
  void openEndedTierInTheMiddleRejected() {
    assertRejected(
        List.of(
            RateTier.bounded(0, 100, 1_000),
            RateTier.openEnded(100, 1_500),
            RateTier.openEnded(200, 2_000)),
        "Only the last tier");
  }

  @Test
  void boundedLastTierRejected() {
    assertRejected(
        List.of(RateTier.bounded(0, 100, 1_000), RateTier.bounded(100, 200, 1_500)),
        "last tier must have no upper limit");
  }

  @Test
  void tierMustEndAfterItStarts() {
    assertThatThrownBy(() -> RateTier.bounded(100, 100, 1_000))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("to")).isPresent());
    assertThatThrownBy(() -> RateTier.bounded(100, 50, 1_000))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void tierRateMustBePositive() {
    assertThatThrownBy(() -> RateTier.bounded(0, 100, 0))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("ratePerMetre")).isPresent());
    assertThatThrownBy(() -> RateTier.openEnded(0, -1)).isInstanceOf(ValidationException.class);
  }

  @Test
  void tierRejectsNulls() {
    assertThatThrownBy(() -> new RateTier(null, Optional.empty(), Money.of(1)))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new RateTier(Metres.ZERO, null, Money.of(1)))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new RateTier(Metres.ZERO, Optional.empty(), null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void tierLabels() {
    assertThat(RateTier.bounded(0, 100, 1_000).label()).isEqualTo("0–100 m");
    assertThat(RateTier.bounded(100, 200, 1_500).label()).isEqualTo("100–200 m");
    assertThat(
            new RateTier(Metres.of("50.5"), Optional.of(Metres.of("75.25")), Money.of(1)).label())
        .isEqualTo("50.5–75.25 m");
    assertThat(RateTier.openEnded(300, 2_500).label()).isEqualTo("300 m and above");
  }

  private static void assertRejected(List<RateTier> tiers, String messageFragment) {
    assertThatThrownBy(() -> new RateTable(tiers))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.messageFor("tiers").orElseThrow()).contains(messageFragment));
  }
}
