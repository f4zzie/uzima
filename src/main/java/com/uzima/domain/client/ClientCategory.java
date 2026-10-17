package com.uzima.domain.client;

/** Client category; drives the survey and authority fees and the client ID prefix. */
public enum ClientCategory {
  INDUSTRIAL("IND", "Industrial"),
  COMMERCIAL("COM", "Commercial"),
  DOMESTIC("DOM", "Domestic");

  private final String code;
  private final String label;

  ClientCategory(String code, String label) {
    this.code = code;
    this.label = label;
  }

  /** Three-letter code used in client IDs, e.g. {@code DOM}. */
  public String code() {
    return code;
  }

  public String label() {
    return label;
  }

  public static ClientCategory fromCode(String code) {
    for (ClientCategory c : values()) {
      if (c.code.equals(code)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Unknown client category code: " + code);
  }
}
