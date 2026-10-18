package com.uzima.ui.component;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Marks the focus owner with {@code :keyboard-focus} when focus arrived by keyboard (Tab, arrow
 * keys, shortcuts) rather than by a click.
 *
 * <p>JavaFX's own {@code :focus-visible} is set only by Tab traversal, so it disappears when a
 * component moves focus itself in response to arrow keys, as the sidebar does.
 */
public final class KeyboardFocus {

  public static final PseudoClass KEYBOARD_FOCUS = PseudoClass.getPseudoClass("keyboard-focus");
  private static final String INSTALLED = KeyboardFocus.class.getName();

  private KeyboardFocus() {}

  /** Installs the tracking on {@code scene}; calling it again does nothing. */
  public static void install(Scene scene) {
    if (scene.getProperties().putIfAbsent(INSTALLED, Boolean.TRUE) != null) {
      return;
    }
    boolean[] lastInputWasKeyboard = {false};
    scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> lastInputWasKeyboard[0] = true);
    scene.addEventFilter(
        MouseEvent.MOUSE_PRESSED,
        e -> {
          lastInputWasKeyboard[0] = false;
          // Clicking the node that already has focus doesn't move focus, so clear it here too.
          Node owner = scene.getFocusOwner();
          if (owner != null) {
            owner.pseudoClassStateChanged(KEYBOARD_FOCUS, false);
          }
        });
    scene
        .focusOwnerProperty()
        .addListener(
            (obs, old, now) -> {
              if (old != null) {
                old.pseudoClassStateChanged(KEYBOARD_FOCUS, false);
              }
              if (now != null) {
                now.pseudoClassStateChanged(KEYBOARD_FOCUS, lastInputWasKeyboard[0]);
              }
            });
  }

  public static boolean hasKeyboardFocus(Node node) {
    return node.getPseudoClassStates().contains(KEYBOARD_FOCUS);
  }
}
