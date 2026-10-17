package com.uzima.domain.client;

import com.uzima.domain.common.Validator;
import java.util.Objects;
import java.util.Optional;

/** The editable, validated part of a client. */
public record ClientDetails(
    String name,
    ClientCategory category,
    PhoneNumber phone,
    Optional<EmailAddress> email,
    County county,
    String address,
    Optional<GpsLocation> location) {

  public static final int MAX_NAME = 120;
  public static final int MAX_ADDRESS = 255;

  public ClientDetails {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(category, "category");
    Objects.requireNonNull(phone, "phone");
    Objects.requireNonNull(county, "county");
    address = address == null ? "" : address;
    email = email == null ? Optional.empty() : email;
    location = location == null ? Optional.empty() : location;
  }

  /**
   * Validates raw form input and reports every invalid field at once. Blank email and blank
   * coordinates mean "not given".
   */
  public static ClientDetails parse(
      String name,
      ClientCategory category,
      String phone,
      String email,
      County county,
      String address,
      String latitude,
      String longitude) {
    Validator v = new Validator();
    String trimmedName = name == null ? "" : name.trim();
    v.require(trimmedName, "name", "Name is required")
        .check(trimmedName.length() <= MAX_NAME, "name", "Name is too long")
        .check(category != null, "category", "Choose a category")
        .check(county != null, "county", "Choose a county");
    String trimmedAddress = address == null ? "" : address.trim();
    v.check(trimmedAddress.length() <= MAX_ADDRESS, "address", "Address is too long");

    PhoneNumber[] parsedPhone = new PhoneNumber[1];
    v.capture(() -> parsedPhone[0] = PhoneNumber.parse(phone));

    EmailAddress[] parsedEmail = new EmailAddress[1];
    if (email != null && !email.isBlank()) {
      v.capture(() -> parsedEmail[0] = EmailAddress.parse(email));
    }

    GpsLocation[] parsedLocation = new GpsLocation[1];
    boolean hasLat = latitude != null && !latitude.isBlank();
    boolean hasLon = longitude != null && !longitude.isBlank();
    if (hasLat != hasLon) {
      v.add(hasLat ? "longitude" : "latitude", "Enter both latitude and longitude, or neither");
    } else if (hasLat) {
      v.capture(() -> parsedLocation[0] = GpsLocation.parse(latitude, longitude));
    }

    v.throwIfInvalid();
    return new ClientDetails(
        trimmedName,
        category,
        parsedPhone[0],
        Optional.ofNullable(parsedEmail[0]),
        county,
        trimmedAddress,
        Optional.ofNullable(parsedLocation[0]));
  }

  /** True when coordinates were given and fall outside Kenya; the UI shows a warning. */
  public boolean hasLocationOutsideKenya() {
    return location.map(l -> !l.isWithinKenya()).orElse(false);
  }
}
