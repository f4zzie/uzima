package com.uzima.app;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Resolves the per-user application data directory, where the H2 database, logs and the optional
 * {@code uzima.properties} override live.
 *
 * <p>Precedence: {@code UZIMA_HOME} environment variable, then the {@code uzima.home} system
 * property, then the platform convention.
 */
public final class AppDirectories {

  static final String HOME_ENV = "UZIMA_HOME";
  static final String HOME_PROPERTY = "uzima.home";

  private AppDirectories() {}

  public static Path resolve() {
    return resolve(System.getenv(), System.getProperties());
  }

  static Path resolve(Map<String, String> env, Properties sys) {
    String override = env.get(HOME_ENV);
    if (isBlank(override)) {
      override = sys.getProperty(HOME_PROPERTY);
    }
    if (!isBlank(override)) {
      return Path.of(override).toAbsolutePath().normalize();
    }

    Path userHome = Path.of(sys.getProperty("user.home"));
    String os = sys.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    if (os.contains("win")) {
      String appData = env.get("APPDATA");
      return (isBlank(appData) ? userHome.resolve("AppData").resolve("Roaming") : Path.of(appData))
          .resolve("Uzima");
    }
    if (os.contains("mac")) {
      return userHome.resolve("Library").resolve("Application Support").resolve("Uzima");
    }
    String xdg = env.get("XDG_DATA_HOME");
    return (isBlank(xdg) ? userHome.resolve(".local").resolve("share") : Path.of(xdg))
        .resolve("uzima");
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
