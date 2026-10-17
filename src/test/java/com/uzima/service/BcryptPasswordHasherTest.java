package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BcryptPasswordHasherTest {

  private final BcryptPasswordHasher hasher = new BcryptPasswordHasher(4);

  @Test
  void producesSaltedBcryptHashesThatVerify() {
    String a = hasher.hash("correct horse".toCharArray());
    String b = hasher.hash("correct horse".toCharArray());

    assertThat(a).startsWith("$2a$04$").hasSize(60).isNotEqualTo(b).doesNotContain("correct");
    assertThat(hasher.matches("correct horse".toCharArray(), a)).isTrue();
    assertThat(hasher.matches("correct horsE".toCharArray(), a)).isFalse();
  }

  @Test
  void defaultCostIsTwelve() {
    assertThat(BcryptPasswordHasher.DEFAULT_COST).isEqualTo(12);
    assertThat(new BcryptPasswordHasher().hash("abcdefghij".toCharArray())).startsWith("$2a$12$");
  }

  @Test
  void malformedOrMissingInputsNeverMatch() {
    assertThat(hasher.matches("x".toCharArray(), "not-a-hash")).isFalse();
    assertThat(hasher.matches(null, hasher.hash("abc".toCharArray()))).isFalse();
    assertThat(hasher.matches("abc".toCharArray(), null)).isFalse();
  }

  @Test
  void passwordsLongerThan72BytesAreFullySignificant() {
    String base = "a".repeat(80);
    String hash = hasher.hash((base + "1").toCharArray());
    assertThat(hasher.matches((base + "1").toCharArray(), hash)).isTrue();
    assertThat(hasher.matches((base + "2").toCharArray(), hash)).isFalse();
  }

  @Test
  void rejectsCostOutsideBcryptRange() {
    assertThatThrownBy(() -> new BcryptPasswordHasher(3))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new BcryptPasswordHasher(32))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
