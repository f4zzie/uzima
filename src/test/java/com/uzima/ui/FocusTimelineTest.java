package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

class FocusTimelineTest {
  @Test
  void tracingIsOptInAndDoesNotTakeFocus() {
    withProperty(
        "false",
        () ->
            FxTest.onFx(
                () -> {
                  Scene scene = new Scene(new VBox(new TextField()));
                  FxTest.traceFocus(scene);
                  FxTest.markFocus(scene, "navigation");
                  assertThat(FocusTimeline.dump(scene)).isEmpty();
                  assertThat(scene.getFocusOwner()).isNull();
                }));
  }

  @Test
  void recordsOwnerTransitionsCallerAndReadinessInOrder() {
    withProperty(
        "true",
        () ->
            FxTest.onFx(
                () -> {
                  TextField old = new TextField();
                  old.setId("old");
                  TextField next = new TextField();
                  next.setId("next");
                  Scene scene = new Scene(new VBox(old, next));
                  FxTest.traceFocus(scene);
                  FxTest.traceFocus(scene);
                  old.requestFocus();
                  FxTest.markFocus(scene, "navigator.open sent");
                  FxTest.markFocus(scene, "editor READY");
                  next.requestFocus();
                  String timeline = FocusTimeline.dump(scene);
                  assertThat(timeline)
                      .contains(
                          "Focus timeline at ",
                          " ms focus:",
                          "id=old",
                          "id=next",
                          "FocusTimelineTest.java:")
                      .containsOnlyOnce("attached;")
                      .containsSubsequence(
                          "navigator.open sent", "editor READY", " -> TextField[id=next");
                  assertThat(scene.getFocusOwner()).isSameAs(next);
                }));
  }

  private static void withProperty(String value, Runnable test) {
    String previous = System.getProperty("uzima.focusTimeline");
    System.setProperty("uzima.focusTimeline", value);
    try {
      test.run();
    } finally {
      if (previous == null) {
        System.clearProperty("uzima.focusTimeline");
      } else {
        System.setProperty("uzima.focusTimeline", previous);
      }
    }
  }
}
