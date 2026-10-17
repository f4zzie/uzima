package com.uzima.domain.client;

import com.uzima.domain.common.ValidationException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The business identifier shown on screens and documents: {@code UZM-{IND|COM|DOM}-{NNNN}},
 * sequential per category. Numbers past 9999 widen rather than wrap.
 */
public record ClientCode(ClientCategory category, int sequence) {

  private static final Pattern FORMAT = Pattern.compile("UZM-(IND|COM|DOM)-(\\d{4,9})");

  public ClientCode {
    Objects.requireNonNull(category, "category");
    if (sequence < 1) {
      throw new IllegalArgumentException("Client sequence must be positive: " + sequence);
    }
  }

  public static ClientCode parse(String text) {
    Matcher m = FORMAT.matcher(text == null ? "" : text.trim());
    if (!m.matches()) {
      throw new ValidationException("clientCode", "Client IDs look like UZM-DOM-0042");
    }
    return new ClientCode(ClientCategory.fromCode(m.group(1)), Integer.parseInt(m.group(2)));
  }

  public String value() {
    return "UZM-%s-%04d".formatted(category.code(), sequence);
  }

  @Override
  public String toString() {
    return value();
  }
}
