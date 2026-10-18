package com.uzima.ui.component;

import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Switches a screen area between its content and the standard loading, empty and error states, so
 * every screen handles them the same way. The states are drawn by the same {@link EmptyState} that
 * tables use as their placeholder, so they look identical everywhere.
 */
public class StatePane extends StackPane {

  /** What the pane is currently showing. */
  public enum State {
    CONTENT,
    LOADING,
    EMPTY,
    ERROR
  }

  private final Node content;
  private final EmptyState panel = new EmptyState();
  private final Button emptyAction = new Button();
  private State state = State.CONTENT;
  private String message = "";

  public StatePane(Node content) {
    this.content = Objects.requireNonNull(content);
    getStyleClass().add("state-pane");
    emptyAction.getStyleClass().add("primary");
    getChildren().addAll(content, panel);
    apply(State.CONTENT, "");
  }

  public void showContent() {
    apply(State.CONTENT, "");
  }

  public void showLoading(String text) {
    String shown = text == null ? "Loading…" : text;
    panel.showLoading(shown);
    apply(State.LOADING, shown);
  }

  /** Empty state with no action button. */
  public void showEmpty(String title, String description) {
    showEmpty(title, description, null, null);
  }

  /** Empty state with a call to action, e.g. "Add the first client". */
  public void showEmpty(String title, String description, String actionLabel, Runnable action) {
    boolean hasAction = actionLabel != null && action != null;
    emptyAction.setText(hasAction ? actionLabel : "");
    emptyAction.setOnAction(hasAction ? e -> action.run() : null);
    emptyAction.setVisible(hasAction);
    panel.showMessage(title, description, hasAction ? emptyAction : null);
    apply(State.EMPTY, title);
  }

  /** Error state; "Try again" calls {@code retry}, and is hidden when it is null. */
  public void showError(String text, Runnable retry) {
    panel.showError(text, retry);
    apply(State.ERROR, text);
  }

  public State getState() {
    return state;
  }

  public Node getContent() {
    return content;
  }

  public Button retryButton() {
    return panel.retryButton();
  }

  public Button emptyActionButton() {
    return emptyAction;
  }

  /** The loading text, empty-state title or error message currently shown; empty for content. */
  public String getMessage() {
    return message;
  }

  private void apply(State next, String text) {
    state = next;
    message = text;
    boolean showContent = next == State.CONTENT;
    content.setVisible(showContent);
    content.setManaged(showContent);
    panel.setVisible(!showContent);
    panel.setManaged(!showContent);
  }
}
