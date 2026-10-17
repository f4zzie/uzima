package com.uzima.domain.user;

import com.uzima.domain.common.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/** A login name: trimmed, lower-case, 3–32 characters of {@code a-z 0-9 . _ -}. */
public record Username(String value) {

  private static final Pattern FORMAT = Pattern.compile("[a-z0-9._-]{3,32}");

  public Username {
    String normalised = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (normalised.isEmpty()) {
      throw new ValidationException("username", "Username is required");
    }
    if (!FORMAT.matcher(normalised).matches()) {
      throw new ValidationException(
          "username", "Use 3–32 letters, digits, dots, dashes or underscores");
    }
    value = normalised;
  }

  @Override
  public String toString() {
    return value;
  }
}
