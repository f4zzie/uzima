package com.uzima.ui.component;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

/**
 * Makes a node ignore the keyboard while something covers it. Used by overlays: mouse input is
 * stopped by the overlay itself, keys by this filter, so nothing under a dialog or loading overlay
 * can be typed into or activated.
 */
final class InputBlocker {

  private final Node covered;
  private final EventHandler<KeyEvent> filter = KeyEvent::consume;
  private int holds;

  InputBlocker(Node covered) {
    this.covered = covered;
  }

  /** Blocks keys; calls nest, so two overlays each need their own {@link #release()}. */
  void block() {
    if (holds++ == 0) {
      covered.addEventFilter(KeyEvent.ANY, filter);
    }
  }

  void release() {
    if (holds > 0 && --holds == 0) {
      covered.removeEventFilter(KeyEvent.ANY, filter);
    }
  }

  boolean isBlocking() {
    return holds > 0;
  }

  /** True when focus is currently on {@code covered} or inside it. */
  boolean containsFocus() {
    if (covered.getScene() == null) {
      return false;
    }
    for (Node n = covered.getScene().getFocusOwner(); n != null; n = n.getParent()) {
      if (n == covered) {
        return true;
      }
    }
    return false;
  }
}
