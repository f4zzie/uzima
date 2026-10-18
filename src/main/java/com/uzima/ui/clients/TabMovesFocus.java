package com.uzima.ui.clients;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Makes Tab and Shift+Tab leave a text area instead of typing a tab character, so a form with an
 * address box can be filled in by keyboard alone. (A short address never needs a tab character.)
 */
final class TabMovesFocus {

  private TabMovesFocus() {}

  static void install(TextArea area) {
    area.addEventFilter(
        KeyEvent.KEY_PRESSED,
        e -> {
          if (e.getCode() != KeyCode.TAB || e.isControlDown() || e.isAltDown()) {
            return;
          }
          e.consume();
          // The text area's own behaviour moves focus on Ctrl+Tab and Ctrl+Shift+Tab.
          area.fireEvent(
              new KeyEvent(
                  KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, e.isShiftDown(), true, false, false));
        });
  }
}
