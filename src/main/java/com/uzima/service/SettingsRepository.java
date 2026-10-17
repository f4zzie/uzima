package com.uzima.service;

import java.util.Map;

/** Key/value company settings. */
public interface SettingsRepository {

  /** Every stored setting; keys with no row are absent from the map. */
  Map<SettingKey, String> load();

  /** Inserts or replaces one value. */
  void save(SettingKey key, String value);
}
