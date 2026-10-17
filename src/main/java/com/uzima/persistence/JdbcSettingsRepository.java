package com.uzima.persistence;

import com.uzima.service.SettingKey;
import com.uzima.service.SettingsRepository;
import java.util.EnumMap;
import java.util.Map;

public final class JdbcSettingsRepository implements SettingsRepository {

  private final Jdbc jdbc;

  public JdbcSettingsRepository(Jdbc jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public Map<SettingKey, String> load() {
    Map<SettingKey, String> values = new EnumMap<>(SettingKey.class);
    for (SettingKey key : SettingKey.values()) {
      jdbc.queryOne(
              "SELECT setting_value FROM settings WHERE setting_key = ?",
              rs -> rs.getString(1),
              key.key())
          .ifPresent(v -> values.put(key, v));
    }
    return values;
  }

  @Override
  public void save(SettingKey key, String value) {
    int rows =
        jdbc.update(
            "UPDATE settings SET setting_value = ? WHERE setting_key = ?", value, key.key());
    if (rows == 0) {
      jdbc.update(
          "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)", key.key(), value);
    }
  }
}
