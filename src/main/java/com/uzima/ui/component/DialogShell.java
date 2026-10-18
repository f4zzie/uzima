package com.uzima.ui.component;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The frame every dialog uses: title and close button, a scrolling body, a form-level banner, and a
 * footer with Cancel and one primary (or danger) action. Shown in-window by {@link
 * OverlayHost#showDialog}.
 *
 * <p>Keyboard: Esc cancels, Enter runs the primary action (except in a multi-line text area, and a
 * focused button fires itself), and Tab stays inside the dialog.
 *
 * <p>Without a primary handler the primary button simply closes the dialog as {@link
 * Outcome#CONFIRMED}. With one, the handler decides: typically it calls {@link #setBusy}, runs the
 * service call, then {@link #close} on success or {@link #showError} on failure.
 */
public class DialogShell extends VBox {

  /** Dialog widths. */
  public enum Size {
    SMALL,
    MEDIUM,
    LARGE;

    String styleClass() {
      return "dialog-" + name().toLowerCase(Locale.ROOT);
    }
  }

  /** How a dialog was closed. */
  public enum Outcome {
    CONFIRMED,
    CANCELLED
  }

  private final Label title = new Label();
  private final Label subtitle = new Label();
  private final Button closeButton = new Button("×");
  private final Banner banner = new Banner();
  private final ScrollPane body = new FittingScrollPane();
  private final ProgressIndicator busyIndicator = new ProgressIndicator();
  private final Button cancel = new Button("Cancel");
  private final Button primary = new Button("Save");
  private final CompletableFuture<Outcome> result = new CompletableFuture<>();
  private Consumer<DialogShell> onPrimary;
  private Consumer<Outcome> onClose = o -> {};
  private boolean busy;

  public DialogShell(String titleText) {
    this(titleText, Size.MEDIUM);
  }

  public DialogShell(String titleText, Size size) {
    getStyleClass().addAll("dialog", size.styleClass());
    setAccessibleRole(AccessibleRole.DIALOG);
    setAccessibleText(titleText);

    title.setText(titleText);
    title.getStyleClass().addAll("text-title", "dialog-title");
    title.setWrapText(true);
    subtitle.getStyleClass().addAll("text-muted", "dialog-subtitle");
    subtitle.setWrapText(true);
    setSubtitle(null);
    VBox headings = new VBox(title, subtitle);
    headings.getStyleClass().add("dialog-headings");
    HBox.setHgrow(headings, Priority.ALWAYS);
    closeButton.getStyleClass().addAll("ghost", "dialog-close");
    closeButton.setAccessibleText("Close dialog");
    closeButton.setOnAction(e -> cancel());
    HBox header = new HBox(headings, closeButton);
    header.getStyleClass().add("dialog-header");

    body.getStyleClass().add("dialog-body");
    body.setFitToWidth(true);
    body.setFocusTraversable(false);
    body.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    VBox.setVgrow(body, Priority.ALWAYS);

    busyIndicator.getStyleClass().add("dialog-busy");
    busyIndicator.setVisible(false);
    busyIndicator.setAccessibleText("Working");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    cancel.setCancelButton(false);
    cancel.setOnAction(e -> cancel());
    primary.getStyleClass().add("primary");
    primary.setOnAction(e -> runPrimary());
    HBox footer = new HBox(spacer, busyIndicator, cancel, primary);
    footer.getStyleClass().add("dialog-footer");

    VBox top = new VBox(header, banner);
    top.getStyleClass().add("dialog-top");
    getChildren().addAll(top, body, footer);

    addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
    FocusTrap.install(this);
  }

  public DialogShell setSubtitle(String text) {
    boolean show = text != null && !text.isBlank();
    subtitle.setText(show ? text : "");
    subtitle.setVisible(show);
    subtitle.setManaged(show);
    return this;
  }

  public DialogShell setContent(Node content) {
    body.setContent(content);
    return this;
  }

  public Node getContent() {
    return body.getContent();
  }

  /** Sets the primary action's label; {@code danger} styles it red for destructive actions. */
  public DialogShell setPrimary(String label, boolean danger) {
    primary.setText(label);
    primary.getStyleClass().removeAll("primary", "danger");
    primary.getStyleClass().add(danger ? "danger" : "primary");
    return this;
  }

  public DialogShell setCancelLabel(String label) {
    cancel.setText(label);
    return this;
  }

  /** Runs when the primary action is chosen; the handler closes the dialog when it's done. */
  public DialogShell setOnPrimary(Consumer<DialogShell> handler) {
    onPrimary = handler;
    return this;
  }

  /** Runs once when the dialog closes, on either outcome. */
  public DialogShell setOnClose(Consumer<Outcome> handler) {
    onClose = handler == null ? o -> {} : handler;
    return this;
  }

  /** Completes with the outcome when the dialog closes. */
  public CompletionStage<Outcome> result() {
    return result;
  }

  public void showError(String message) {
    banner.showError(message);
  }

  public void clearError() {
    banner.hide();
  }

  public Banner banner() {
    return banner;
  }

  /** While busy, actions and the body are disabled and a spinner shows; Esc is ignored. */
  public void setBusy(boolean value) {
    busy = value;
    primary.setDisable(value);
    cancel.setDisable(value);
    closeButton.setDisable(value);
    body.setDisable(value);
    busyIndicator.setVisible(value);
    if (!value && getScene() != null && getScene().getFocusOwner() == null) {
      focusInitial();
    }
  }

  public boolean isBusy() {
    return busy;
  }

  public void setPrimaryDisabled(boolean disabled) {
    primary.setDisable(disabled || busy);
  }

  /** Cancels unless busy: closes with {@link Outcome#CANCELLED}. */
  public void cancel() {
    if (!busy) {
      close(Outcome.CANCELLED);
    }
  }

  /** Closes the dialog with {@code outcome}. Calling it again has no effect. */
  public void close(Outcome outcome) {
    if (result.isDone()) {
      return;
    }
    busy = false;
    onClose.accept(outcome);
    result.complete(outcome);
  }

  public boolean isClosed() {
    return result.isDone();
  }

  public String getTitle() {
    return title.getText();
  }

  public Button primaryButton() {
    return primary;
  }

  public Button cancelButton() {
    return cancel;
  }

  public Button closeButton() {
    return closeButton;
  }

  /** Focuses the first input in the body, or the primary button if there is none. */
  public void focusInitial() {
    Node first =
        body.getContent() instanceof javafx.scene.Parent p ? FocusTrap.firstInput(p) : null;
    (first != null ? first : primary).requestFocus();
  }

  void runPrimary() {
    if (busy || primary.isDisabled()) {
      return;
    }
    if (onPrimary == null) {
      close(Outcome.CONFIRMED);
    } else {
      onPrimary.accept(this);
    }
  }

  private void onKey(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      cancel();
      event.consume();
    } else if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
      Node owner = getScene() == null ? null : getScene().getFocusOwner();
      if (owner instanceof TextArea) {
        return;
      }
      if (owner instanceof ButtonBase button) {
        button.fire();
      } else {
        runPrimary();
      }
      event.consume();
    }
  }

  /**
   * A scroll pane whose preferred height is its content's height at the actual width, so wrapped
   * text gets its full height and the dialog only scrolls when it truly doesn't fit the window.
   */
  private static final class FittingScrollPane extends ScrollPane {

    @Override
    public Orientation getContentBias() {
      return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefHeight(double width) {
      Node content = getContent();
      if (content == null) {
        return super.computePrefHeight(width);
      }
      Insets insets = getInsets();
      double outer = width > 0 ? width : getWidth() > 0 ? getWidth() : prefWidth(-1);
      double inner = Math.max(0, outer - insets.getLeft() - insets.getRight());
      return insets.getTop() + content.prefHeight(inner) + insets.getBottom();
    }
  }
}
