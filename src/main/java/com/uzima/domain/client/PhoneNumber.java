package com.uzima.domain.client;

import com.uzima.domain.common.ValidationException;
import java.util.regex.Pattern;

/**
 * A Kenyan mobile number normalised to E.164: {@code +2547XXXXXXXX} or {@code +2541XXXXXXXX}.
 * Accepts the common ways people type them: {@code 0712 345 678}, {@code 712345678}, {@code
 * 254712345678}, {@code +254-712-345-678}.
 */
public record PhoneNumber(String value) {

  private static final Pattern E164 = Pattern.compile("\\+254[17]\\d{8}");
  static final String FIELD = "phone";

  public PhoneNumber {
    if (value == null || !E164.matcher(value).matches()) {
      throw new ValidationException(FIELD, "Enter a Kenyan mobile number, e.g. 0712 345 678");
    }
  }

  public static PhoneNumber parse(String input) {
    if (input == null || input.isBlank()) {
      throw new ValidationException(FIELD, "Phone number is required");
    }
    String digits = input.trim().replaceAll("[\\s\\-().]", "");
    if (digits.startsWith("+")) {
      digits = digits.substring(1);
    } else if (digits.startsWith("00")) {
      digits = digits.substring(2);
    }
    if (!digits.chars().allMatch(Character::isDigit)) {
      throw new ValidationException(FIELD, "Phone numbers can only contain digits");
    }
    String national;
    if (digits.startsWith("254") && digits.length() == 12) {
      national = digits.substring(3);
    } else if (digits.startsWith("0") && digits.length() == 10) {
      national = digits.substring(1);
    } else if (digits.length() == 9) {
      national = digits;
    } else {
      throw new ValidationException(FIELD, "Enter a Kenyan mobile number, e.g. 0712 345 678");
    }
    return new PhoneNumber("+254" + national);
  }

  /** {@code 0712 345 678} for display. */
  public String formatLocal() {
    String n = value.substring(4);
    return "0" + n.substring(0, 3) + " " + n.substring(3, 6) + " " + n.substring(6);
  }

  @Override
  public String toString() {
    return value;
  }
}
