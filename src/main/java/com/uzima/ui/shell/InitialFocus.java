package com.uzima.ui.shell;

import java.util.Optional;
import javafx.scene.Node;

/** A page's focus destination once navigation and rendering have finished. */
public interface InitialFocus {
  /** The initial control, or {@code null} while the page is still loading. */
  Node initialFocusTarget();

  /** Called once by the shell per navigation, after the target is visible and enabled. */
  default void requestInitialFocus() {
    initialFocusTarget().requestFocus();
  }

  static <N extends Node> N attach(N page, InitialFocus focus) {
    page.getProperties().put(InitialFocus.class, focus);
    return page;
  }

  static Optional<InitialFocus> of(Node page) {
    if (page instanceof InitialFocus self) {
      return Optional.of(self);
    }
    return page.getProperties().get(InitialFocus.class) instanceof InitialFocus focus
        ? Optional.of(focus)
        : Optional.empty();
  }
}
