package com.uzima.service;

/**
 * The only keys the settings table may hold. Persistence binds {@link #key()} as a parameter, so no
 * setting name ever comes from user input.
 */
public enum SettingKey {
  COMPANY_NAME("company.name"),
  COMPANY_ADDRESS("company.address"),
  COMPANY_PHONE("company.phone"),
  COMPANY_EMAIL("company.email"),
  COMPANY_KRA_PIN("company.kra_pin"),
  COMPANY_LOGO_PATH("company.logo_path"),
  TAX_RATE("tax.rate"),
  IDLE_LOCK_MINUTES("session.idle_lock_minutes"),
  EXPORT_FOLDER("export.default_folder");

  private final String key;

  SettingKey(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }
}
