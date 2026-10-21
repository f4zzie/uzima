package com.uzima.ui.component;

import java.util.Locale;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A short notification shown in the corner of the window by {@link OverlayHost#toast}. Success and
 * info toasts hide themselves; errors stay until dismissed, so they can't be missed.
 */
public class Toast extends HBox {

  /** Toast colour schemes; each maps to a style class in theme.css. */
  public enum Kind {
    SUCCESS,
    INFO,
    ERROR;

    String styleClass() {
      return "toast-" + name().toLowerCase(Locale.ROOT);
    }

    /** Errors stay on screen until the user dismisses them. */
    public boolean autoHides() {
      return this != ERROR;
    }
  }

  private final Kind kind;
  private final Label title = new Label();
  private final Label message = new Label();
  private final Button dismiss = new Button("×");
  private Runnable onDismiss = () -> {};

  public Toast(Kind kind, String titleText, String messageText) {
    this.kind = kind;
    getStyleClass().addAll("toast", kind.styleClass());
    title.setText(titleText);
    title.getStyleClass().add("toast-title");
    message.setText(messageText == null ? "" : messageText);
    message.getStyleClass().add("toast-message");
    message.setWrapText(true);
    message.setVisible(!message.getText().isEmpty());
    message.setManaged(message.isVisible());
    VBox text = new VBox(title, message);
    text.getStyleClass().add("toast-text");
    HBox.setHgrow(text, Priority.ALWAYS);
    dismiss.getStyleClass().addAll("ghost", "small", "toast-dismiss");
    dismiss.setAccessibleText("Dismiss notification");
    dismiss.setOnAction(e -> onDismiss.run());
    getChildren().addAll(text, dismiss);
    setAccessibleRole(AccessibleRole.TEXT);
    setAccessibleText(message.getText().isEmpty() ? titleText : titleText + ". " + messageText);
  }

  public Kind kind() {
    return kind;
  }

  public String getTitle() {
    return title.getText();
  }

  public String getMessage() {
    return message.getText();
  }

  /** The dismiss ("×") button, for tests and keyboard focus. */
  public Button dismissButton() {
    return dismiss;
  }

  void setOnDismiss(Runnable action) {
    onDismiss = action;
  }
}
