package com.uzima.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UsernameTest {

  @Test
  void trimsAndLowerCases() {
    assertThat(new Username("  Jane.Wanjiru ").value()).isEqualTo("jane.wanjiru");
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "a_b-c.d", "user123", "abcdefghijklmnopqrstuvwxyz012345"})
  void acceptsValidNames(String name) {
    assertThat(new Username(name).value()).isEqualTo(name);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void requiresAValue(String name) {
    assertThatThrownBy(() -> new Username(name))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.messageFor("username")).contains("Username is required"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"ab", "abcdefghijklmnopqrstuvwxyz0123456", "jane wanjiru", "jane@uzima", "jané"})
  void rejectsInvalidNames(String name) {
    assertThatThrownBy(() -> new Username(name))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("username")).isPresent());
  }
}
