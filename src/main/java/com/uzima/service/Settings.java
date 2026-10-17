package com.uzima.service;

import com.uzima.domain.client.EmailAddress;
import com.uzima.domain.common.Validator;
import com.uzima.domain.money.TaxRate;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Company details printed on documents, plus app-wide options. Blank strings mean "not set"; an
 * empty {@code logoPath} means no logo.
 */
public record Settings(
    String companyName,
    String address,
    String phone,
    String email,
    String kraPin,
    String logoPath,
    TaxRate taxRate,
    int idleLockMinutes,
    String exportFolder) {

  public static final int MIN_IDLE_MINUTES = 1;
  public static final int MAX_IDLE_MINUTES = 240;

  /** KRA PINs are a letter, nine digits and a letter, e.g. P051234567X. */
  private static final Pattern KRA_PIN = Pattern.compile("[A-Z]\\d{9}[A-Z]");

  public Settings {
    companyName = clean(companyName);
    address = clean(address);
    phone = clean(phone);
    email = clean(email);
    kraPin = clean(kraPin).toUpperCase(Locale.ROOT);
    logoPath = clean(logoPath);
    exportFolder = clean(exportFolder);
    Objects.requireNonNull(taxRate, "taxRate");
  }

  /** Throws a {@link com.uzima.domain.common.ValidationException} listing every invalid field. */
  public void validate() {
    Validator v = new Validator();
    v.require(companyName, "companyName", "Company name is required")
        .check(companyName.length() <= 120, "companyName", "Company name is too long")
        .check(address.length() <= 255, "address", "Address is too long")
        .check(phone.length() <= 40, "phone", "Phone is too long")
        .check(
            kraPin.isEmpty() || KRA_PIN.matcher(kraPin).matches(),
            "kraPin",
            "KRA PINs look like P051234567X")
        .check(
            idleLockMinutes >= MIN_IDLE_MINUTES && idleLockMinutes <= MAX_IDLE_MINUTES,
            "idleLockMinutes",
            "Idle lock must be between "
                + MIN_IDLE_MINUTES
                + " and "
                + MAX_IDLE_MINUTES
                + " minutes")
        .check(logoPath.length() <= 1000, "logoPath", "Logo path is too long")
        .check(exportFolder.length() <= 1000, "exportFolder", "Folder path is too long");
    if (!email.isEmpty()) {
      v.capture(() -> EmailAddress.parse(email));
    }
    v.throwIfInvalid();
  }

  private static String clean(String s) {
    return s == null ? "" : s.strip();
  }
}
