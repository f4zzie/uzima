package com.uzima.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidatorTest {

  @Test
  void collectsEveryFailedCheck() {
    Validator v =
        new Validator()
            .check(false, "a", "A is wrong")
            .check(true, "b", "never")
            .require("  ", "c", "C is required")
            .require(null, "d", "D is required")
            .require("ok", "e", "never")
            .add("f", "F is wrong");

    assertThat(v.hasErrors()).isTrue();
    assertThatThrownBy(v::throwIfInvalid)
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> {
              assertThat(e.errors())
                  .extracting(FieldError::field)
                  .containsExactly("a", "c", "d", "f");
              assertThat(e.getMessage())
                  .isEqualTo("A is wrong; C is required; D is required; F is wrong");
              assertThat(e.messageFor("c")).contains("C is required");
              assertThat(e.messageFor("zzz")).isEmpty();
            });
  }

  @Test
  void noErrorsMeansNoThrow() {
    Validator v = new Validator().check(true, "a", "x");
    assertThat(v.hasErrors()).isFalse();
    assertThatCode(v::throwIfInvalid).doesNotThrowAnyException();
  }

  @Test
  void captureTurnsValidationExceptionsIntoCollectedErrors() {
    Validator v =
        new Validator()
            .capture(
                () -> {
                  throw new ValidationException(
                      List.of(new FieldError("x", "X bad"), new FieldError("y", "Y bad")));
                })
            .capture(() -> {})
            .check(false, "z", "Z bad");

    assertThatThrownBy(v::throwIfInvalid)
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(FieldError::field)
                    .containsExactly("x", "y", "z"));
  }

  @Test
  void captureDoesNotSwallowOtherExceptions() {
    Validator v = new Validator();
    assertThatThrownBy(
            () ->
                v.capture(
                    () -> {
                      throw new IllegalStateException("boom");
                    }))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void singleFieldConstructorAndDefensiveCopy() {
    ValidationException e = new ValidationException("name", "Name is required");
    assertThat(e.errors()).containsExactly(new FieldError("name", "Name is required"));
    assertThat(e.getMessage()).isEqualTo("Name is required");
    assertThat(e).isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> e.errors().add(new FieldError("a", "b")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void emptyErrorListHasGenericMessage() {
    assertThat(new ValidationException(List.of()).getMessage()).isEqualTo("Invalid input");
  }

  @Test
  void fieldErrorRejectsNulls() {
    assertThatThrownBy(() -> new FieldError(null, "m")).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new FieldError("f", null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void displayFormats() {
    assertThat(DisplayFormats.ZONE.getId()).isEqualTo("Africa/Nairobi");
    assertThat(DisplayFormats.DATE.format(LocalDate.of(2026, 3, 7))).isEqualTo("07/03/2026");
    assertThat(DisplayFormats.DATE_TIME.format(LocalDateTime.of(2026, 3, 7, 9, 5)))
        .isEqualTo("07/03/2026 09:05");
  }
}
