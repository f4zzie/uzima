package com.uzima.app;

import com.uzima.persistence.DatabaseConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Layered application configuration.
 *
 * <ol>
 *   <li>{@code application.properties} on the classpath (defaults, committed)
 *   <li>{@code uzima.properties} in the app data directory (optional, per installation)
 *   <li>Environment variables {@code UZIMA_DB_URL}, {@code UZIMA_DB_USER}, {@code
 *       UZIMA_DB_PASSWORD}
 * </ol>
 *
 * <p>Later layers win. The token {@code ${app.data}} in any value is replaced with the app data
 * directory.
 */
public final class AppConfig {

  static final String DEFAULTS_RESOURCE = "/application.properties";
  static final String OVERRIDE_FILE = "uzima.properties";
  static final String APP_DATA_TOKEN = "${app.data}";

  private static final Map<String, String> ENV_OVERRIDES =
      Map.of(
          "UZIMA_DB_URL", "uzima.db.url",
          "UZIMA_DB_USER", "uzima.db.user",
          "UZIMA_DB_PASSWORD", "uzima.db.password");

  private final Path dataDir;
  private final Properties properties;

  private AppConfig(Path dataDir, Properties properties) {
    this.dataDir = dataDir;
    this.properties = properties;
  }

  public static AppConfig load(Path dataDir) {
    return load(dataDir, System.getenv());
  }

  static AppConfig load(Path dataDir, Map<String, String> env) {
    Properties props = new Properties();
    try (InputStream in = AppConfig.class.getResourceAsStream(DEFAULTS_RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("Missing " + DEFAULTS_RESOURCE + " on the classpath");
      }
      props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read " + DEFAULTS_RESOURCE, e);
    }

    Path overrideFile = dataDir.resolve(OVERRIDE_FILE);
    if (Files.isRegularFile(overrideFile)) {
      try (Reader reader = Files.newBufferedReader(overrideFile, StandardCharsets.UTF_8)) {
        props.load(reader);
      } catch (IOException e) {
        throw new UncheckedIOException("Cannot read " + overrideFile, e);
      }
    }

    ENV_OVERRIDES.forEach(
        (envName, key) -> {
          String value = env.get(envName);
          if (value != null && !value.isBlank()) {
            props.setProperty(key, value);
          }
        });

    return new AppConfig(dataDir, props);
  }

  public Path dataDir() {
    return dataDir;
  }

  public String get(String key) {
    String value = properties.getProperty(key);
    if (value == null) {
      return null;
    }
    // Forward slashes are valid in JDBC URLs and file paths on every supported OS.
    return value.replace(APP_DATA_TOKEN, dataDir.toString().replace('\\', '/')).trim();
  }

  public String require(String key) {
    String value = get(key);
    if (value == null || value.isEmpty()) {
      throw new IllegalStateException("Missing required configuration: " + key);
    }
    return value;
  }

  public int getInt(String key, int defaultValue) {
    String value = get(key);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Configuration " + key + " must be an integer", e);
    }
  }

  public DatabaseConfig database() {
    return new DatabaseConfig(
        require("uzima.db.url"),
        get("uzima.db.user"),
        get("uzima.db.password"),
        getInt("uzima.db.pool.max-size", 5));
  }
}
