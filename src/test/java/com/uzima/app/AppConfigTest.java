package com.uzima.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.persistence.DatabaseConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

  @TempDir Path dataDir;

  @Test
  void defaultsPointAtEmbeddedH2InsideAppData() {
    DatabaseConfig db = AppConfig.load(dataDir, Map.of()).database();

    String expectedPrefix =
        "jdbc:h2:file:" + dataDir.toString().replace('\\', '/') + "/data/uzima;";
    assertThat(db.url()).startsWith(expectedPrefix).contains("MODE=MySQL");
    assertThat(db.isH2()).isTrue();
    assertThat(db.maxPoolSize()).isEqualTo(5);
  }

  @Test
  void overrideFileReplacesDefaults() throws IOException {
    Files.writeString(
        dataDir.resolve(AppConfig.OVERRIDE_FILE),
        "uzima.db.url=jdbc:mysql://localhost/uzima\nuzima.db.pool.max-size=8\n");

    DatabaseConfig db = AppConfig.load(dataDir, Map.of()).database();

    assertThat(db.url()).isEqualTo("jdbc:mysql://localhost/uzima");
    assertThat(db.maxPoolSize()).isEqualTo(8);
  }

  @Test
  void environmentOverridesFileAndDefaults() throws IOException {
    Files.writeString(
        dataDir.resolve(AppConfig.OVERRIDE_FILE), "uzima.db.url=jdbc:mysql://file/uzima\n");

    DatabaseConfig db =
        AppConfig.load(
                dataDir,
                Map.of(
                    "UZIMA_DB_URL", "jdbc:mysql://env/uzima",
                    "UZIMA_DB_USER", "env-user",
                    "UZIMA_DB_PASSWORD", "env-secret"))
            .database();

    assertThat(db.url()).isEqualTo("jdbc:mysql://env/uzima");
    assertThat(db.user()).isEqualTo("env-user");
    assertThat(db.password()).isEqualTo("env-secret");
  }

  @Test
  void blankEnvironmentValuesAreIgnored() {
    DatabaseConfig db = AppConfig.load(dataDir, Map.of("UZIMA_DB_URL", "  ")).database();
    assertThat(db.url()).startsWith("jdbc:h2:file:");
  }

  @Test
  void nonNumericPoolSizeIsReported() throws IOException {
    Files.writeString(dataDir.resolve(AppConfig.OVERRIDE_FILE), "uzima.db.pool.max-size=lots\n");
    AppConfig config = AppConfig.load(dataDir, Map.of());
    assertThatThrownBy(config::database).hasMessageContaining("uzima.db.pool.max-size");
  }

  @Test
  void missingRequiredKeyIsReported() {
    AppConfig config = AppConfig.load(dataDir, Map.of());
    assertThatThrownBy(() -> config.require("no.such.key")).hasMessageContaining("no.such.key");
  }
}
