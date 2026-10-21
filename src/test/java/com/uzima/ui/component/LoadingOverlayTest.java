package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

class LoadingOverlayTest {

  private final FxRobot robot = new FxRobot();
  private final AtomicInteger clicks = new AtomicInteger();
  private LoadingOverlay overlay;
  private TextField field;
  private Button button;
  private Scene scene;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    var root =
        FxTest.onFx(
            () -> {
              field = new TextField();
              button = new Button("Save");
              button.setOnAction(e -> clicks.incrementAndGet());
              overlay = new LoadingOverlay();
              return overlay.wrap(new VBox(field, button));
            });
    scene = ComponentStage.show(root);
  }

  @Test
  void blocksInputAtOnceButRevealsOnlyAfterTheDelay() throws Exception {
    FxTest.onFx(
        () -> {
          field.requestFocus();
          overlay.setShowDelay(Duration.millis(300));
          overlay.show("Loading clients…");
        });
    assertThat(overlay.isShowing()).isTrue();
    assertThat(overlay.isRevealed()).isFalse();

    robot.write("abc");
    robot.clickOn(button);
    assertThat(field.getText()).isEmpty();
    assertThat(clicks).hasValue(0);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, overlay::isRevealed);
    assertThat(overlay.getMessage()).isEqualTo("Loading clients…");
    assertThat(overlay.getAccessibleText()).isEqualTo("Loading clients…");
  }

  @Test
  void hidingBeforeTheDelayNeverReveals() {
    FxTest.onFx(
        () -> {
          overlay.setShowDelay(Duration.seconds(5));
          overlay.show("Saving…");
          overlay.hide();
        });
    assertThat(overlay.isShowing()).isFalse();
    assertThat(overlay.isRevealed()).isFalse();
  }

  @Test
  void hideRestoresInputAndFocus() {
    FxTest.onFx(
        () -> {
          field.requestFocus();
          overlay.setShowDelay(Duration.ZERO);
          overlay.show("Saving…");
          overlay.show("Still saving…");
        });
    assertThat(overlay.isRevealed()).isTrue();
    assertThat(overlay.getMessage()).isEqualTo("Still saving…");

    FxTest.onFx(overlay::hide);
    FxTest.onFx(() -> overlay.hide());
    assertThat(scene.getFocusOwner()).isSameAs(field);
    robot.write("ok");
    assertThat(field.getText()).isEqualTo("ok");
  }
}
