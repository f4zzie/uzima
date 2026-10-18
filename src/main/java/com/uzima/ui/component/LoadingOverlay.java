package com.uzima.ui.component;

import javafx.animation.PauseTransition;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Covers a region with a spinner and a message while work runs. Input to the region is blocked as
 * soon as {@link #show} is called; the spinner itself only appears after a short delay, so quick
 * calls don't flash.
 */
public class LoadingOverlay extends StackPane {

  /** How long work may run before the spinner is shown. */
  public static final Duration DEFAULT_SHOW_DELAY = Duration.millis(150);

  private final Label message = new Label();
  private final ProgressIndicator spinner = new ProgressIndicator();
  private final PauseTransition reveal = new PauseTransition(DEFAULT_SHOW_DELAY);
  private InputBlocker blocker;
  private Node focusBefore;

  public LoadingOverlay() {
    getStyleClass().add("loading-overlay");
    spinner.getStyleClass().add("loading-spinner");
    message.getStyleClass().add("loading-message");
    message.setWrapText(true);
    VBox panel = new VBox(spinner, message);
    panel.getStyleClass().add("loading-panel");
    getChildren().add(panel);
    setAccessibleRole(AccessibleRole.TEXT);
    setFocusTraversable(false);
    reveal.setOnFinished(e -> getStyleClass().add("revealed"));
    setVisible(false);
    // Swallow clicks on the overlay so they never reach the covered region.
    addEventHandler(javafx.scene.input.MouseEvent.ANY, javafx.event.Event::consume);
  }

  /**
   * Covers {@code content}: returns a stack holding the content with this overlay above it, and
   * blocks the keyboard for {@code content} while the overlay shows.
   */
  public StackPane wrap(Node content) {
    cover(content);
    return new StackPane(content, this);
  }

  /** Blocks keys to {@code content} while showing; use when the stack is built elsewhere. */
  public void cover(Node content) {
    blocker = new InputBlocker(content);
  }

  public void setShowDelay(Duration delay) {
    reveal.setDuration(delay);
  }

  public void show(String text) {
    message.setText(text == null ? "" : text);
    setAccessibleText(text);
    if (isVisible()) {
      return;
    }
    if (blocker != null) {
      if (blocker.containsFocus()) {
        focusBefore = getScene().getFocusOwner();
      }
      blocker.block();
    }
    setVisible(true);
    toFront();
    if (reveal.getDuration().lessThanOrEqualTo(Duration.ZERO)) {
      getStyleClass().add("revealed");
    } else {
      reveal.playFromStart();
    }
  }

  public void hide() {
    if (!isVisible()) {
      return;
    }
    reveal.stop();
    getStyleClass().remove("revealed");
    setVisible(false);
    if (blocker != null) {
      blocker.release();
    }
    if (focusBefore != null && focusBefore.getScene() != null) {
      focusBefore.requestFocus();
    }
    focusBefore = null;
  }

  /** True from {@link #show} until {@link #hide}: input is blocked during this time. */
  public boolean isShowing() {
    return isVisible();
  }

  /** True once the spinner and message are visible (after the show delay). */
  public boolean isRevealed() {
    return getStyleClass().contains("revealed");
  }

  public String getMessage() {
    return message.getText();
  }
}
