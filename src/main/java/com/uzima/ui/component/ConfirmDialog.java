package com.uzima.ui.component;

import java.util.function.BiConsumer;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * A yes/no question in a {@link DialogShell}, optionally asking for a reason (cancelling or
 * reopening a job, reversing a payment). A missing reason shows inline under the reason field.
 */
public final class ConfirmDialog {

  public static final String REASON_REQUIRED = "A reason is required";

  private final DialogShell shell;
  private final Label message = new Label();
  private FormField reasonField;
  private TextArea reasonInput;
  private BiConsumer<DialogShell, String> onConfirm;

  public ConfirmDialog(String title, String messageText) {
    shell = new DialogShell(title, DialogShell.Size.SMALL);
    message.setText(messageText);
    message.setWrapText(true);
    message.getStyleClass().add("confirm-message");
    shell.setContent(new VBox(message));
    shell.setPrimary("Confirm", false);
    shell.setOnPrimary(d -> confirm());
  }

  public ConfirmDialog confirmLabel(String label, boolean danger) {
    shell.setPrimary(label, danger);
    return this;
  }

  public ConfirmDialog cancelLabel(String label) {
    shell.setCancelLabel(label);
    return this;
  }

  /** Adds a required multi-line reason field with the given label. */
  public ConfirmDialog requireReason(String label) {
    reasonInput = new TextArea();
    reasonInput.setPrefRowCount(3);
    reasonInput.setWrapText(true);
    reasonField = new FormField(label, reasonInput);
    reasonField.setName("reason");
    reasonField.setRequired(true);
    reasonInput.textProperty().addListener((obs, old, now) -> reasonField.clearError());
    VBox content = new VBox(message, reasonField);
    content.getStyleClass().add("confirm-content");
    shell.setContent(content);
    return this;
  }

  /**
   * Runs on confirm with the trimmed reason (empty when none is asked for). The handler owns the
   * dialog from then on: set it busy, call the service, then close it or show the error. Without a
   * handler, confirming just closes the dialog.
   */
  public ConfirmDialog onConfirm(BiConsumer<DialogShell, String> handler) {
    onConfirm = handler;
    return this;
  }

  public DialogShell shell() {
    return shell;
  }

  /** The trimmed reason entered so far, or empty. */
  public String reason() {
    return reasonInput == null ? "" : reasonInput.getText().strip();
  }

  public FormField reasonField() {
    return reasonField;
  }

  private void confirm() {
    if (reasonField != null && reason().isEmpty()) {
      reasonField.setError(REASON_REQUIRED);
      reasonInput.requestFocus();
      return;
    }
    if (onConfirm == null) {
      shell.close(DialogShell.Outcome.CONFIRMED);
    } else {
      onConfirm.accept(shell, reason());
    }
  }
}
