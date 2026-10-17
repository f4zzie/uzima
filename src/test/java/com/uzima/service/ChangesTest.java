package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.money.Money;
import org.junit.jupiter.api.Test;

class ChangesTest {

  @Test
  void listsOnlyFieldsThatChanged() {
    Changes changes =
        new Changes()
            .field("price", Money.of(130_000), Money.of(140_000))
            .field("name", "Core", "Core")
            .field("active", true, false);

    assertThat(changes.isEmpty()).isFalse();
    assertThat(changes.toString())
        .isEqualTo("price: KSh 130,000.00 → KSh 140,000.00; active: true → false");
  }

  @Test
  void nullIsShownAsNone() {
    assertThat(new Changes().field("email", null, "a@b.co").toString())
        .isEqualTo("email: (none) → a@b.co");
    assertThat(new Changes().field("email", "a@b.co", null).toString())
        .isEqualTo("email: a@b.co → (none)");
  }

  @Test
  void noChangesIsEmpty() {
    Changes changes = new Changes().field("x", 1, 1).field("y", null, null);
    assertThat(changes.isEmpty()).isTrue();
    assertThat(changes.toString()).isEmpty();
  }
}
