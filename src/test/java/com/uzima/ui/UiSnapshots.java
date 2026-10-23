package com.uzima.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 * Renders the app's own scenes to PNG with {@link Scene#snapshot}, under headless Monocle. It never
 * captures the screen. Files go to {@code target/ui-snapshots/} for review and, later, the README.
 */
public final class UiSnapshots {

  /** The two window sizes every screen is checked at. */
  public static final List<int[]> SIZES = List.of(new int[] {1366, 768}, new int[] {1280, 720});

  private UiSnapshots() {}

  public static Path directory() {
    return Path.of(System.getProperty("uzima.snapshotDir", "target/ui-snapshots"));
  }

  /** Builds a fresh root for each standard size and saves {@code name-WxH.png}. */
  public static List<Path> saveAtStandardSizes(String name, Supplier<? extends Parent> root) {
    return SIZES.stream().map(s -> save(name, root.get(), s[0], s[1])).toList();
  }

  /** Lays out {@code root} in a themed scene of the given size and saves it. */
  public static Path save(String name, Parent root, double width, double height) {
    return FxTest.onFx(
        () -> {
          Scene scene = UiTheme.apply(new Scene(root, width, height));
          return write(scene, name + "-" + (int) width + "x" + (int) height);
        });
  }

  /** Saves an already-built scene (e.g. one shown in a test stage) under {@code name}. */
  public static Path save(Scene scene, String name) {
    return FxTest.onFx(() -> write(scene, name));
  }

  /** Saves {@code root} at {@code width} and its own preferred height, for long pages. */
  public static Path saveFullHeight(String name, Parent root, double width) {
    return FxTest.onFx(
        () -> {
          Scene measuring = UiTheme.apply(new Scene(root));
          root.applyCss();
          double height = Math.ceil(root.prefHeight(width));
          measuring.setRoot(new Group());
          Scene sized = UiTheme.apply(new Scene(root, width, height));
          return write(sized, name + "-full");
        });
  }

  private static Path write(Scene scene, String fileName) {
    scene.getRoot().applyCss();
    scene.getRoot().layout();
    WritableImage image = scene.snapshot(null);
    Path file = directory().resolve(fileName + ".png");
    try {
      Files.createDirectories(file.getParent());
      ImageIO.write(toBufferedImage(image), "png", file.toFile());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return file;
  }

  private static BufferedImage toBufferedImage(WritableImage image) {
    int w = (int) image.getWidth();
    int h = (int) image.getHeight();
    BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    PixelReader pixels = image.getPixelReader();
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        out.setRGB(x, y, pixels.getArgb(x, y));
      }
    }
    return out;
  }
}
