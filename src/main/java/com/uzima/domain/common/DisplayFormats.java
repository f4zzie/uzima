package com.uzima.domain.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Display conventions shared by screens, documents and exports. */
public final class DisplayFormats {

  public static final ZoneId ZONE = ZoneId.of("Africa/Nairobi");
  public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private DisplayFormats() {}
}
