package com.uzima.domain.common;

import java.util.Objects;

/** A validation failure tied to a named input field, so the UI can show it next to that field. */
public record FieldError(String field, String message) {

  public FieldError {
    Objects.requireNonNull(field, "field");
    Objects.requireNonNull(message, "message");
  }
}
