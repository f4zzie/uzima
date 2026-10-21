package com.uzima.ui.component;

import java.util.Locale;
import java.util.function.Function;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;

/**
 * Lets a non-editable combo box jump to an item by typing the start of its name, as native lists
 * do: "na" selects Nairobi, then "nak" Nakuru. Typing pauses longer than a second start over.
 */
public final class TypeAhead {

  private static final long RESET_NANOS = 1_000_000_000L;

  private TypeAhead() {}

  public static <T> void install(ComboBox<T> box, Function<T, String> label) {
    StringBuilder typed = new StringBuilder();
    long[] last = {0};
    box.addEventHandler(
        KeyEvent.KEY_TYPED,
        e -> {
          String ch = e.getCharacter();
          if (ch.isEmpty() || !Character.isLetterOrDigit(ch.charAt(0))) {
            return;
          }
          long now = System.nanoTime();
          if (now - last[0] > RESET_NANOS) {
            typed.setLength(0);
          }
          last[0] = now;
          typed.append(ch.toLowerCase(Locale.ROOT));
          String prefix = typed.toString();
          box.getItems().stream()
              .filter(item -> label.apply(item).toLowerCase(Locale.ROOT).startsWith(prefix))
              .findFirst()
              .ifPresent(
                  item -> {
                    box.getSelectionModel().select(item);
                    e.consume();
                  });
        });
  }
}
