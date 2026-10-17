package com.uzima.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds the "old → new" text stored in audit details, listing only fields that changed, e.g.
 * {@code price: KSh 130,000.00 → KSh 140,000.00; active: true → false}.
 */
public final class Changes {

  private final List<String> parts = new ArrayList<>();

  public Changes field(String name, Object before, Object after) {
    if (!Objects.equals(before, after)) {
      parts.add(name + ": " + text(before) + " → " + text(after));
    }
    return this;
  }

  public boolean isEmpty() {
    return parts.isEmpty();
  }

  @Override
  public String toString() {
    return String.join("; ", parts);
  }

  private static String text(Object value) {
    return value == null ? "(none)" : value.toString();
  }
}
