package com.uzima.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.text.Font;

/** Loads the bundled Inter fonts and applies the single design-system stylesheet to scenes. */
public final class UiTheme {

  private static final List<String> FONT_WEIGHTS = List.of("Regular", "Medium", "SemiBold", "Bold");
  private static final String STYLESHEET = "css/theme.css";

  private static volatile boolean fontsLoaded;

  private UiTheme() {}

  /** Registers Inter with JavaFX. Safe to call more than once; only the first call loads. */
  public static synchronized void loadFonts() {
    if (fontsLoaded) {
      return;
    }
    for (String weight : FONT_WEIGHTS) {
      String resource = "fonts/Inter-" + weight + ".ttf";
      try (InputStream in = UiTheme.class.getResourceAsStream(resource)) {
        if (in == null || Font.loadFont(in, 14) == null) {
          throw new IllegalStateException("Cannot load bundled font " + resource);
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    fontsLoaded = true;
  }

  public static String stylesheet() {
    return Objects.requireNonNull(UiTheme.class.getResource(STYLESHEET), STYLESHEET)
        .toExternalForm();
  }

  /** Loads fonts if needed and puts the theme on {@code scene}. */
  public static Scene apply(Scene scene) {
    loadFonts();
    if (!scene.getStylesheets().contains(stylesheet())) {
      scene.getStylesheets().add(stylesheet());
    }
    return scene;
  }
}
