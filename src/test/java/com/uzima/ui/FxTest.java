package com.uzima.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

/** Starts the headless JavaFX toolkit once and runs code on the FX thread for tests. */
public final class FxTest {

  private FxTest() {}

  public static void startToolkit() {
    try {
      FxToolkit.registerPrimaryStage();
    } catch (TimeoutException e) {
      throw new IllegalStateException("JavaFX toolkit did not start", e);
    }
    UiTheme.loadFonts();
  }

  /** Runs {@code work} on the FX thread and returns its result, rethrowing its exception. */
  public static <T> T onFx(Callable<T> work) {
    startToolkit();
    try {
      return WaitForAsyncUtils.asyncFx(work).get(30, TimeUnit.SECONDS);
    } catch (java.util.concurrent.ExecutionException e) {
      if (e.getCause() instanceof RuntimeException re) {
        throw re;
      }
      throw new IllegalStateException(e.getCause());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static void onFx(Runnable work) {
    onFx(
        () -> {
          work.run();
          return null;
        });
  }

  /** Waits until queued FX events (including Backend callbacks) have run. */
  public static void settle() {
    WaitForAsyncUtils.waitForFxEvents();
  }

  /** Activates the window once, then waits for native and scene focus before keyboard input. */
  public static void ensureStageReadyForInput(Stage stage) {
    activate(stage);
    awaitFocus(stage, "a non-null scene focus owner", () -> null, false);
  }

  /** Does not request node focus: initial-focus and tab-order bugs must still fail the test. */
  public static void ensureStageReadyForInput(Stage stage, String expectedQuery) {
    activate(stage);
    waitForFocus(stage, expectedQuery);
  }

  public static void waitForFocus(Stage stage, String expectedQuery) {
    awaitFocus(
        stage,
        expectedQuery,
        () -> stage.getScene() == null ? null : stage.getScene().lookup(expectedQuery),
        true);
  }

  public static void waitForFocus(Stage stage, Node expectedNode) {
    awaitFocus(stage, onFx(() -> describe(expectedNode)), () -> expectedNode, true);
  }

  private static void activate(Stage stage) {
    onFx(
        () -> {
          traceFocus(stage.getScene());
          markFocus(stage.getScene(), "stage activation requested");
          stage.show();
          stage.toFront();
          stage.requestFocus();
        });
  }

  private static void awaitFocus(
      Stage stage, String expected, Supplier<Node> expectedNode, boolean requireExactNode) {
    try {
      WaitForAsyncUtils.waitFor(
          15,
          TimeUnit.SECONDS,
          () ->
              onFx(
                  () -> {
                    Scene scene = stage.getScene();
                    Node node = expectedNode.get();
                    return stage.isShowing()
                        && stage.isFocused()
                        && scene != null
                        && scene.getFocusOwner() != null
                        && (!requireExactNode
                            || (node != null && scene.getFocusOwner() == node && node.isFocused()));
                  }));
    } catch (TimeoutException e) {
      throw new AssertionError(
          "Expected keyboard focus on "
              + expected
              + " within 15 seconds; "
              + onFx(
                  () ->
                      focusState(stage, expectedNode.get()) + FocusTimeline.dump(stage.getScene())),
          e);
    }
  }

  static String focusState(Stage stage, Node expectedNode) {
    Scene scene = stage.getScene();
    return "stage.isShowing()="
        + stage.isShowing()
        + ", stage.isFocused()="
        + stage.isFocused()
        + ", scene.getFocusOwner()="
        + describe(scene == null ? null : scene.getFocusOwner())
        + ", expectedNode="
        + describe(expectedNode);
  }

  /** Call on the FX thread before showing the scene to capture startup as well as navigation. */
  public static void traceFocus(Scene scene) {
    if (scene != null) {
      FocusTimeline.attach(scene);
    }
  }

  /** Test-only marker for navigation and asynchronous page readiness. */
  public static void markFocus(Scene scene, String event) {
    FocusTimeline.mark(scene, event);
  }

  static String describe(Node node) {
    return node == null
        ? "null"
        : node.getClass().getSimpleName()
            + "[id="
            + node.getId()
            + ", focused="
            + node.isFocused()
            + ", visible="
            + node.isVisible()
            + ", disabled="
            + node.isDisabled()
            + "]";
  }
}
