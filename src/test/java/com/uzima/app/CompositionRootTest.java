package com.uzima.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompositionRootTest {

  @TempDir Path dataDir;

  @Test
  void startCreatesEmbeddedDatabaseFileInAppDataDirectory() {
    try (CompositionRoot root =
        CompositionRoot.start(AppConfig.load(dataDir.resolve("nested"), Map.of()))) {
      assertThat(root.databaseDescription()).startsWith("H2 ");
      assertThat(root.config().dataDir()).isEqualTo(dataDir.resolve("nested"));
    }
    assertThat(Files.exists(dataDir.resolve("nested/data/uzima.mv.db"))).isTrue();
  }
}
