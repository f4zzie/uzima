package com.uzima.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class AppDirectoriesTest {

  private static Properties sys(String os, String home) {
    Properties p = new Properties();
    p.setProperty("os.name", os);
    p.setProperty("user.home", home);
    return p;
  }

  @Test
  void envOverrideWinsOverSystemProperty() {
    Properties p = sys("Linux", "/home/a");
    p.setProperty(AppDirectories.HOME_PROPERTY, "/from/property");
    Path dir = AppDirectories.resolve(Map.of(AppDirectories.HOME_ENV, "/from/env"), p);
    assertThat(dir).isEqualTo(Path.of("/from/env").toAbsolutePath());
  }

  @Test
  void systemPropertyOverridesPlatformDefault() {
    Properties p = sys("Linux", "/home/a");
    p.setProperty(AppDirectories.HOME_PROPERTY, "/from/property");
    assertThat(AppDirectories.resolve(Map.of(), p))
        .isEqualTo(Path.of("/from/property").toAbsolutePath());
  }

  @Test
  void linuxUsesXdgDataHome() {
    assertThat(AppDirectories.resolve(Map.of(), sys("Linux", "/home/a")))
        .isEqualTo(Path.of("/home/a/.local/share/uzima"));
    assertThat(AppDirectories.resolve(Map.of("XDG_DATA_HOME", "/xdg"), sys("Linux", "/home/a")))
        .isEqualTo(Path.of("/xdg/uzima"));
  }

  @Test
  void windowsUsesAppData() {
    assertThat(AppDirectories.resolve(Map.of("APPDATA", "/roaming"), sys("Windows 11", "/u")))
        .isEqualTo(Path.of("/roaming/Uzima"));
  }

  @Test
  void macUsesApplicationSupport() {
    assertThat(AppDirectories.resolve(Map.of(), sys("Mac OS X", "/Users/a")))
        .isEqualTo(Path.of("/Users/a/Library/Application Support/Uzima"));
  }
}
