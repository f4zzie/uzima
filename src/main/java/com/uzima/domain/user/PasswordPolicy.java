package com.uzima.domain.user;

import com.uzima.domain.common.ValidationException;
import java.util.Locale;

/**
 * Password rules: at least {@value #MIN_LENGTH} characters, no composition rules, and not the same
 * as the username. Length is counted in Unicode code points.
 *
 * <p>{@link #strengthOf(String)} is only a hint for the UI and never blocks a valid password.
 */
public final class PasswordPolicy {

  public static final int MIN_LENGTH = 10;

  /** Hint levels shown next to the password field. */
  public enum Strength {
    TOO_SHORT,
    WEAK,
    FAIR,
    STRONG
  }

  private PasswordPolicy() {}

  public static void validate(String password) {
    if (password == null || length(password) < MIN_LENGTH) {
      throw new ValidationException(
          "password", "Password must be at least " + MIN_LENGTH + " characters");
    }
  }

  public static void validate(String password, Username username) {
    validate(password);
    if (username != null && password.toLowerCase(Locale.ROOT).equals(username.value())) {
      throw new ValidationException("password", "Password cannot be the same as the username");
    }
  }

  /**
   * Heuristic: below the minimum is TOO_SHORT. 16+ characters, or 12+ with at least three character
   * classes (lower, upper, digit, other), is STRONG. 12+ characters or two or more classes is FAIR.
   * Anything else that meets the minimum is WEAK.
   */
  public static Strength strengthOf(String password) {
    if (password == null || length(password) < MIN_LENGTH) {
      return Strength.TOO_SHORT;
    }
    int length = length(password);
    int classes = characterClasses(password);
    if (length >= 16 || (length >= 12 && classes >= 3)) {
      return Strength.STRONG;
    }
    if (length >= 12 || classes >= 2) {
      return Strength.FAIR;
    }
    return Strength.WEAK;
  }

  private static int length(String s) {
    return s.codePointCount(0, s.length());
  }

  private static int characterClasses(String s) {
    boolean lower = false;
    boolean upper = false;
    boolean digit = false;
    boolean other = false;
    for (int cp : s.codePoints().toArray()) {
      if (Character.isLowerCase(cp)) {
        lower = true;
      } else if (Character.isUpperCase(cp)) {
        upper = true;
      } else if (Character.isDigit(cp)) {
        digit = true;
      } else {
        other = true;
      }
    }
    return (lower ? 1 : 0) + (upper ? 1 : 0) + (digit ? 1 : 0) + (other ? 1 : 0);
  }
}
