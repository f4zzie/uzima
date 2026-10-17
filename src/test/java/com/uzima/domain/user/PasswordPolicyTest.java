package com.uzima.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.user.PasswordPolicy.Strength;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PasswordPolicyTest {

  @Test
  void nineCharactersRejected() {
    assertThatThrownBy(() -> PasswordPolicy.validate("123456789"))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.messageFor("password"))
                    .contains("Password must be at least 10 characters"));
  }

  @Test
  void tenCharactersAccepted() {
    assertThatCode(() -> PasswordPolicy.validate("1234567890")).doesNotThrowAnyException();
  }

  @Test
  void nullRejected() {
    assertThatThrownBy(() -> PasswordPolicy.validate(null)).isInstanceOf(ValidationException.class);
  }

  @Test
  void lengthCountsCodePointsNotUtf16Units() {
    // Five emoji are ten UTF-16 units but only five characters.
    assertThatThrownBy(() -> PasswordPolicy.validate("😀😀😀😀😀"))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void passwordEqualToUsernameRejectedIgnoringCase() {
    Username username = new Username("wanjiru.k1");
    assertThatThrownBy(() -> PasswordPolicy.validate("WANJIRU.K1", username))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.messageFor("password"))
                    .contains("Password cannot be the same as the username"));
    assertThatCode(() -> PasswordPolicy.validate("wanjiru.k1-borehole", username))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest(name = "\"{0}\" is {1}")
  @CsvSource({
    "short, TOO_SHORT",
    "aaaaaaaaaa, WEAK",
    "aaaaaaaaa1, FAIR",
    "aaaaaaaaaaaa, FAIR",
    "Aaaaaaaaaa1x, STRONG",
    "aaaaaaaaaaaaaaaa, STRONG",
    "correct horse battery, STRONG"
  })
  void strengthHint(String password, Strength expected) {
    assertThat(PasswordPolicy.strengthOf(password)).isEqualTo(expected);
  }

  @Test
  void strengthOfNullIsTooShort() {
    assertThat(PasswordPolicy.strengthOf(null)).isEqualTo(Strength.TOO_SHORT);
  }
}
