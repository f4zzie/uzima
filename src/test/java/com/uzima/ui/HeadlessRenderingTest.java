package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** Guards the virtual-screen/window mismatch that caused Monocle framebuffer overflows. */
@ExtendWith(ApplicationExtension.class)
class HeadlessRenderingTest {

  private Stage stage;

  @Start
  void start(Stage stage) {
    this.stage = stage;
    StackPane root = new StackPane();
    root.setStyle("-fx-background-color: #336699;");
    stage.setScene(new Scene(root, 1366, 768));
    stage.setX(0);
    stage.setY(0);
    stage.show();
    stage.toFront();
    stage.requestFocus();
  }

  @Test
  void virtualScreenContainsStandardWindows() {
    Rectangle2D bounds = FxTest.onFx(() -> Screen.getPrimary().getBounds());
    for (int[] size : UiSnapshots.SIZES) {
      assertThat(bounds.getWidth())
          .as("virtual screen width for %sx%s", size[0], size[1])
          .isGreaterThan(size[0]);
      assertThat(bounds.getHeight())
          .as("virtual screen height for %sx%s", size[0], size[1])
          .isGreaterThan(size[1]);
    }
  }

  @Test
  void shownStageRendersAndSnapshotsAtBothStandardSizes() throws IOException {
    List<Path> files = StageSnapshots.saveAtStandardSizes(stage, "headless-rendering");
    assertThat(files).hasSize(UiSnapshots.SIZES.size()).doesNotHaveDuplicates();
    for (int i = 0; i < files.size(); i++) {
      BufferedImage image = ImageIO.read(files.get(i).toFile());
      assertThat(image).isNotNull();
      int[] size = UiSnapshots.SIZES.get(i);
      assertThat(image.getWidth()).isEqualTo(size[0]);
      assertThat(image.getHeight()).isEqualTo(size[1]);
      assertThat(image.getRGB(size[0] / 2, size[1] / 2)).isEqualTo(0xff336699);
    }
    FxTest.settle();
  }
}
