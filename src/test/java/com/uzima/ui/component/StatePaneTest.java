package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatePaneTest {

  private StatePane pane;
  private Label content;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    pane =
        FxTest.onFx(
            () -> {
              content = new Label("Client list");
              return new StatePane(content);
            });
  }

  @Test
  void startsOnContent() {
    assertThat(pane.getState()).isEqualTo(StatePane.State.CONTENT);
    assertThat(content.isVisible()).isTrue();
    assertThat(pane.getContent()).isSameAs(content);
    assertThat(pane.getMessage()).isEmpty();
  }

  @Test
  void loadingHidesTheContent() {
    FxTest.onFx(() -> pane.showLoading("Loading clients…"));
    assertThat(pane.getState()).isEqualTo(StatePane.State.LOADING);
    assertThat(content.isVisible()).isFalse();
    assertThat(pane.getMessage()).isEqualTo("Loading clients…");

    FxTest.onFx(() -> pane.showLoading(null));
    assertThat(pane.getMessage()).isEqualTo("Loading…");
  }

  @Test
  void emptyStateWithAndWithoutAnAction() {
    AtomicInteger added = new AtomicInteger();
    FxTest.onFx(
        () ->
            pane.showEmpty(
                "No clients yet",
                "Clients you register appear here.",
                "Add client",
                added::incrementAndGet));
    assertThat(pane.getState()).isEqualTo(StatePane.State.EMPTY);
    assertThat(pane.getMessage()).isEqualTo("No clients yet");
    assertThat(pane.emptyActionButton().isVisible()).isTrue();
    FxTest.onFx(() -> pane.emptyActionButton().fire());
    assertThat(added).hasValue(1);

    FxTest.onFx(() -> pane.showEmpty("No matches", null));
    assertThat(pane.emptyActionButton().isVisible()).isFalse();
  }

  @Test
  void errorStateRetries() {
    AtomicInteger retries = new AtomicInteger();
    FxTest.onFx(() -> pane.showError("The database is not reachable.", retries::incrementAndGet));
    assertThat(pane.getState()).isEqualTo(StatePane.State.ERROR);
    assertThat(pane.getMessage()).isEqualTo("The database is not reachable.");
    FxTest.onFx(() -> pane.retryButton().fire());
    assertThat(retries).hasValue(1);

    FxTest.onFx(() -> pane.showError("Not allowed.", null));
    assertThat(pane.retryButton().isVisible()).isFalse();

    FxTest.onFx(pane::showContent);
    assertThat(content.isVisible()).isTrue();
  }
}
