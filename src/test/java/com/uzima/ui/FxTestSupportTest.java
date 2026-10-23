package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

class FxTestSupportTest {

  @AfterEach
  void hideStage() {
    FxTest.onFx(() -> FxToolkit.toolkitContext().getRegisteredStage().hide());
  }

  @Test
  void readinessShowsTheStageAndPreservesTheExpectedFocus() {
    Stage stage = fields();
    FxTest.onFx(
        () -> {
          stage.hide();
          stage.getScene().lookup("#actual").requestFocus();
        });

    FxTest.ensureStageReadyForInput(stage, "#actual");

    assertThat(FxTest.onFx(stage::isShowing)).isTrue();
    assertThat(FxTest.onFx(stage::isFocused)).isTrue();
    assertThat(FxTest.onFx(() -> stage.getScene().getFocusOwner().getId())).isEqualTo("actual");
  }

  @Test
  void wrongFocusStillFailsAndNamesBothNodes() {
    Stage stage = fields();
    FxTest.ensureStageReadyForInput(stage, "#actual");

    assertThatThrownBy(() -> FxTest.waitForFocus(stage, "#expected"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("Expected keyboard focus on #expected within 15 seconds")
        .hasMessageContaining("stage.isShowing()=true")
        .hasMessageContaining("stage.isFocused()=true")
        .hasMessageContaining("scene.getFocusOwner()=TextField[id=actual, focused=true")
        .hasMessageContaining("expectedNode=TextField[id=expected, focused=false");
    assertThat(FxTest.onFx(() -> stage.getScene().getFocusOwner().getId())).isEqualTo("actual");
  }

  @Test
  void testAndSetupFailuresSavePngsBeforeTeardown(@TempDir Path directory) throws Exception {
    String previous = System.getProperty("uzima.snapshotDir");
    System.setProperty("uzima.snapshotDir", directory.toString());
    try {
      EngineTestKit.engine("junit-jupiter")
          .selectors(selectClass(FailureFixture.class))
          .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
          .execute()
          .testEvents()
          .assertStatistics(stats -> stats.started(2).failed(2));

      try (var files = Files.list(directory.resolve("failures"))) {
        var snapshots = files.toList();
        assertThat(snapshots).hasSize(2);
        for (Path snapshot : snapshots) {
          assertThat(snapshot.getFileName().toString()).endsWith(".png");
          var image = ImageIO.read(snapshot.toFile());
          assertThat(image).isNotNull();
          assertThat(image.getWidth()).isEqualTo(1366);
          assertThat(image.getHeight()).isEqualTo(768);
          assertThat(image.getRGB(683, 384)).isEqualTo(0xff336699);
        }
      }
    } finally {
      if (previous == null) {
        System.clearProperty("uzima.snapshotDir");
      } else {
        System.setProperty("uzima.snapshotDir", previous);
      }
    }
  }

  private static Stage fields() {
    return FxTest.onFx(
        () -> {
          Stage stage = FxToolkit.toolkitContext().getRegisteredStage();
          TextField actual = new TextField();
          actual.setId("actual");
          TextField expected = new TextField();
          expected.setId("expected");
          stage.setScene(new Scene(new VBox(actual, expected), 1366, 768));
          stage.sizeToScene();
          stage.show();
          actual.requestFocus();
          return stage;
        });
  }

  @ExtendWith(ApplicationExtension.class)
  static class FailureFixture {
    private Stage stage;

    @Start
    void start(Stage stage) {
      this.stage = stage;
      StackPane root = new StackPane();
      root.setStyle("-fx-background-color: #336699;");
      stage.setScene(new Scene(root, 1366, 768));
      stage.sizeToScene();
      stage.show();
    }

    @BeforeEach
    void failSetup(TestInfo info) {
      if (info.getTestMethod().orElseThrow().getName().equals("setupFailure")) {
        throw new IllegalStateException("Deliberate setup failure to test snapshot capture");
      }
    }

    @AfterEach
    void destroyScene() {
      FxTest.onFx(
          () -> {
            stage.hide();
            stage.setScene(null);
          });
    }

    @Test
    void assertionFailure() {
      throw new AssertionError("Deliberate assertion failure to test snapshot capture");
    }

    @Test
    void setupFailure() {
      throw new AssertionError("Setup must fail before this test runs");
    }
  }
}
