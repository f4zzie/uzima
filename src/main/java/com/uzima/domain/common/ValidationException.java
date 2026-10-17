package com.uzima.domain.common;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** One or more invalid input fields. */
public final class ValidationException extends DomainException {

  private final List<FieldError> errors;

  public ValidationException(List<FieldError> errors) {
    super(
        errors.isEmpty()
            ? "Invalid input"
            : errors.stream().map(FieldError::message).collect(Collectors.joining("; ")));
    this.errors = List.copyOf(errors);
  }

  public ValidationException(String field, String message) {
    this(List.of(new FieldError(field, message)));
  }

  public List<FieldError> errors() {
    return errors;
  }

  /** The first message for {@code field}, if any. */
  public Optional<String> messageFor(String field) {
    return errors.stream()
        .filter(e -> e.field().equals(field))
        .map(FieldError::message)
        .findFirst();
  }
}
