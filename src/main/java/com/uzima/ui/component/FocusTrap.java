package com.uzima.ui.component;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Keeps Tab and Shift+Tab cycling inside one container, as a modal dialog needs. */
final class FocusTrap {

  private FocusTrap() {}

  /** Installs the trap on {@code root}; focus never leaves it through the keyboard. */
  static void install(Parent root) {
    EventHandler<KeyEvent> filter =
        event -> {
          if (event.getCode() != KeyCode.TAB || event.isControlDown() || event.isAltDown()) {
            return;
          }
          List<Node> nodes = focusable(root);
          if (nodes.isEmpty()) {
            event.consume();
            return;
          }
          Node owner = root.getScene() == null ? null : root.getScene().getFocusOwner();
          int index = nodes.indexOf(owner);
          int step = event.isShiftDown() ? -1 : 1;
          int next = index < 0 ? 0 : Math.floorMod(index + step, nodes.size());
          nodes.get(next).requestFocus();
          event.consume();
        };
    root.addEventFilter(KeyEvent.KEY_PRESSED, filter);
  }

  /** Focusable nodes under {@code root} in tree (reading) order. */
  static List<Node> focusable(Parent root) {
    List<Node> out = new ArrayList<>();
    collect(root, out);
    return out;
  }

  /** The first focusable input that isn't a button, if any. */
  static Node firstInput(Parent root) {
    return focusable(root).stream()
        .filter(n -> !(n instanceof javafx.scene.control.ButtonBase))
        .findFirst()
        .orElse(null);
  }

  private static void collect(Parent parent, List<Node> out) {
    for (Node child : parent.getChildrenUnmodifiable()) {
      if (!child.isVisible() || child.isDisabled()) {
        continue;
      }
      if (child.isFocusTraversable()) {
        out.add(child);
        // Controls such as TextArea or ScrollPane own focusable skin parts; don't descend.
        if (child instanceof javafx.scene.control.Control) {
          continue;
        }
      }
      if (child instanceof javafx.scene.control.ScrollPane scroll
          && scroll.getContent() instanceof Parent content) {
        collect(content, out);
      } else if (child instanceof Parent p) {
        collect(p, out);
      }
    }
  }
}
