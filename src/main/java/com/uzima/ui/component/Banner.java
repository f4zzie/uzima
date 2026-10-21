package com.uzima.ui.component;

import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** A form-level message box (errors not tied to one field, notices, confirmations). */
public class Banner extends HBox {

  /** Banner colour schemes; each maps to a style class in theme.css. */
  public enum Kind {
    DANGER,
    INFO,
    SUCCESS,
    WARNING;

    String styleClass() {
      return name().toLowerCase(java.util.Locale.ROOT);
    }
  }

  private final Label message = new Label();

  public Banner() {
    getStyleClass().add("banner");
    message.setWrapText(true);
    message.setMaxWidth(Double.MAX_VALUE);
    getChildren().add(message);
    setAccessibleRole(AccessibleRole.TEXT);
    hide();
  }

  public void show(Kind kind, String text) {
    for (Kind k : Kind.values()) {
      getStyleClass().remove(k.styleClass());
    }
    getStyleClass().add(kind.styleClass());
    message.setText(text);
    setAccessibleText(text);
    setVisible(true);
    setManaged(true);
  }

  public void showError(String text) {
    show(Kind.DANGER, text);
  }

  public void hide() {
    message.setText("");
    setVisible(false);
    setManaged(false);
  }

  public String getText() {
    return message.getText();
  }

  public boolean isShowing() {
    return isVisible();
  }
}
