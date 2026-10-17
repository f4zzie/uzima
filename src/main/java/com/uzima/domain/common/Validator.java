package com.uzima.domain.common;

import java.util.ArrayList;
import java.util.List;

/** Collects field errors so a form reports every problem at once instead of one at a time. */
public final class Validator {

  private final List<FieldError> errors = new ArrayList<>();

  public Validator check(boolean valid, String field, String message) {
    if (!valid) {
      errors.add(new FieldError(field, message));
    }
    return this;
  }

  public Validator require(String value, String field, String message) {
    return check(value != null && !value.isBlank(), field, message);
  }

  public Validator add(String field, String message) {
    errors.add(new FieldError(field, message));
    return this;
  }

  /** Runs {@code step}, turning any {@link ValidationException} it throws into collected errors. */
  public Validator capture(Runnable step) {
    try {
      step.run();
    } catch (ValidationException e) {
      errors.addAll(e.errors());
    }
    return this;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public void throwIfInvalid() {
    if (hasErrors()) {
      throw new ValidationException(errors);
    }
  }
}
