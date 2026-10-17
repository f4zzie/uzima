package com.uzima.domain.client;

import com.uzima.domain.common.Validator;
import java.math.BigDecimal;

/**
 * A latitude/longitude pair in decimal degrees. Coordinates outside Kenya are allowed (a client may
 * be registered from a cross-border site) but flagged by {@link #isWithinKenya()} so the UI can
 * warn.
 */
public record GpsLocation(BigDecimal latitude, BigDecimal longitude) {

  // Rough bounding box around Kenya's borders.
  private static final BigDecimal MIN_LAT = new BigDecimal("-4.90");
  private static final BigDecimal MAX_LAT = new BigDecimal("5.10");
  private static final BigDecimal MIN_LON = new BigDecimal("33.80");
  private static final BigDecimal MAX_LON = new BigDecimal("42.00");

  public GpsLocation {
    new Validator()
        .check(
            latitude != null && between(latitude, BigDecimal.valueOf(-90), BigDecimal.valueOf(90)),
            "latitude",
            "Latitude must be between -90 and 90")
        .check(
            longitude != null
                && between(longitude, BigDecimal.valueOf(-180), BigDecimal.valueOf(180)),
            "longitude",
            "Longitude must be between -180 and 180")
        .throwIfInvalid();
  }

  public static GpsLocation parse(String latitude, String longitude) {
    Validator v = new Validator();
    BigDecimal lat = decimal(latitude, "latitude", v);
    BigDecimal lon = decimal(longitude, "longitude", v);
    v.throwIfInvalid();
    return new GpsLocation(lat, lon);
  }

  public boolean isWithinKenya() {
    return between(latitude, MIN_LAT, MAX_LAT) && between(longitude, MIN_LON, MAX_LON);
  }

  private static boolean between(BigDecimal v, BigDecimal min, BigDecimal max) {
    return v.compareTo(min) >= 0 && v.compareTo(max) <= 0;
  }

  private static BigDecimal decimal(String text, String field, Validator v) {
    try {
      return new BigDecimal(text.trim());
    } catch (NumberFormatException | NullPointerException e) {
      v.add(field, "Enter decimal degrees, e.g. -1.2921");
      return null;
    }
  }

  @Override
  public String toString() {
    return latitude.toPlainString() + ", " + longitude.toPlainString();
  }
}
