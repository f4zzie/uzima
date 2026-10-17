package com.uzima.domain.client;

import com.uzima.domain.common.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/** A syntactically plausible email address, stored lower-case. */
public record EmailAddress(String value) {

  private static final Pattern FORMAT =
      Pattern.compile("[A-Za-z0-9._%+'-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");

  public EmailAddress {
    if (value == null || value.length() > 254 || !FORMAT.matcher(value).matches()) {
      throw new ValidationException(
          "email", "Enter a valid email address, e.g. name@example.co.ke");
    }
    value = value.toLowerCase(Locale.ROOT);
  }

  public static EmailAddress parse(String input) {
    return new EmailAddress(input == null ? null : input.trim());
  }

  @Override
  public String toString() {
    return value;
  }
}
