package com.uzima.ui;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/** Opt-in diagnostics only; never requests focus or changes event delivery. */
final class FocusTimeline {
  private static final String PROPERTY = "uzima.focusTimeline";
  private final long started = System.nanoTime();
  private final StringBuilder events =
      new StringBuilder("Focus timeline at " + Instant.now() + "\n");

  private FocusTimeline(Scene scene) {
    record("attached; owner=" + FxTest.describe(scene.getFocusOwner()));
    scene
        .focusOwnerProperty()
        .addListener(
            (obs, old, now) -> {
              record("focus: " + FxTest.describe(old) + " -> " + FxTest.describe(now));
              Arrays.stream(Thread.currentThread().getStackTrace())
                  .filter(FocusTimeline::usefulFrame)
                  .limit(12)
                  .forEach(frame -> events.append("    at ").append(frame).append('\n'));
            });
    scene.addEventFilter(
        MouseEvent.ANY,
        event -> {
          if (event.getEventType() == MouseEvent.MOUSE_PRESSED
              || event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            record("input " + event.getEventType() + "; target=" + event.getTarget());
          }
        });
    scene.addEventFilter(
        KeyEvent.KEY_PRESSED,
        event -> {
          if (event.isShortcutDown() || event.isAltDown()) {
            record(
                "input KEY_PRESSED "
                    + event.getCode()
                    + "; shortcut="
                    + event.isShortcutDown()
                    + "; alt="
                    + event.isAltDown());
          }
        });
  }

  static void attach(Scene scene) {
    if (Boolean.getBoolean(PROPERTY) && !scene.getProperties().containsKey(FocusTimeline.class)) {
      scene.getProperties().put(FocusTimeline.class, new FocusTimeline(scene));
    }
  }

  static void mark(Scene scene, String event) {
    if (scene != null
        && scene.getProperties().get(FocusTimeline.class) instanceof FocusTimeline t) {
      t.record(event + "; owner=" + FxTest.describe(scene.getFocusOwner()));
    }
  }

  static String dump(Scene scene) {
    return scene != null
            && scene.getProperties().get(FocusTimeline.class) instanceof FocusTimeline t
        ? "\n" + t.events
        : "";
  }

  private void record(String event) {
    events.append(
        String.format(
            Locale.ROOT, "+%.3f ms %s%n", (System.nanoTime() - started) / 1_000_000.0, event));
  }

  private static boolean usefulFrame(StackTraceElement frame) {
    String name = frame.getClassName();
    return !name.equals(FocusTimeline.class.getName())
        && (name.startsWith("com.uzima.")
            || frame.getMethodName().toLowerCase(Locale.ROOT).contains("focus")
            || name.contains(".behavior.")
            || name.contains("Scene$ScenePulseListener"));
  }
}
