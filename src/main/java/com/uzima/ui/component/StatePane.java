package com.uzima.ui.component;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Switches a screen area between its content and the standard loading, empty and error states, so
 * every screen handles them the same way.
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
  private final VBox loading = panel("state-loading");
  private final VBox empty = panel("state-empty");
  private final VBox error = panel("state-error");
  private final Label loadingMessage = new Label();
  private final Label emptyTitle = new Label();
  private final Label emptyDescription = new Label();
  private final Button emptyAction = new Button();
  private final Label errorTitle = new Label("Something went wrong");
  private final Label errorMessage = new Label();
  private final Button retry = new Button("Try again");
  private State state = State.CONTENT;
  private Runnable retryAction = () -> {};

  public StatePane(Node content) {
    this.content = Objects.requireNonNull(content);
    getStyleClass().add("state-pane");

    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().add("loading-spinner");
    loadingMessage.getStyleClass().add("state-message");
    loading.getChildren().addAll(spinner, loadingMessage);

    emptyTitle.getStyleClass().add("text-subtitle");
    emptyDescription.getStyleClass().add("state-message");
    emptyDescription.setWrapText(true);
    emptyAction.getStyleClass().add("primary");
    empty.getChildren().addAll(emptyTitle, emptyDescription, emptyAction);

    errorTitle.getStyleClass().addAll("text-subtitle", "text-danger");
    errorMessage.getStyleClass().add("state-message");
    errorMessage.setWrapText(true);
    retry.setOnAction(e -> retryAction.run());
    error.getChildren().addAll(errorTitle, errorMessage, retry);

    for (Label label : new Label[] {loadingMessage, emptyTitle, emptyDescription, errorMessage}) {
      label.setMinHeight(Region.USE_PREF_SIZE);
    }
    getChildren().addAll(content, loading, empty, error);
    apply(State.CONTENT);
  }

  public void showContent() {
    apply(State.CONTENT);
  }

  public void showLoading(String message) {
    loadingMessage.setText(message == null ? "Loading…" : message);
    loading.setAccessibleText(loadingMessage.getText());
    apply(State.LOADING);
  }

  /** Empty state with no action button. */
  public void showEmpty(String title, String description) {
    showEmpty(title, description, null, null);
  }

  /** Empty state with a call to action, e.g. "Add the first client". */
  public void showEmpty(String title, String description, String actionLabel, Runnable action) {
    emptyTitle.setText(title);
    emptyDescription.setText(description == null ? "" : description);
    boolean hasAction = actionLabel != null && action != null;
    emptyAction.setText(hasAction ? actionLabel : "");
    emptyAction.setOnAction(hasAction ? e -> action.run() : null);
    emptyAction.setVisible(hasAction);
    emptyAction.setManaged(hasAction);
    apply(State.EMPTY);
  }

  /** Error state; "Try again" calls {@code retry}. */
  public void showError(String message, Runnable retry) {
    errorMessage.setText(message);
    error.setAccessibleText(errorTitle.getText() + ". " + message);
    retryAction = retry == null ? () -> {} : retry;
    this.retry.setVisible(retry != null);
    this.retry.setManaged(retry != null);
    apply(State.ERROR);
  }

  public State getState() {
    return state;
  }

  public Node getContent() {
    return content;
  }

  public Button retryButton() {
    return retry;
  }

  public Button emptyActionButton() {
    return emptyAction;
  }

  public String getMessage() {
    return switch (state) {
      case CONTENT -> "";
      case LOADING -> loadingMessage.getText();
      case EMPTY -> emptyTitle.getText();
      case ERROR -> errorMessage.getText();
    };
  }

  private void apply(State next) {
    state = next;
    show(content, next == State.CONTENT);
    show(loading, next == State.LOADING);
    show(empty, next == State.EMPTY);
    show(error, next == State.ERROR);
  }

  private static void show(Node node, boolean visible) {
    node.setVisible(visible);
    node.setManaged(visible);
  }

  private static VBox panel(String styleClass) {
    VBox box = new VBox();
    box.getStyleClass().addAll("state-panel", styleClass);
    box.setAlignment(Pos.CENTER);
    box.setAccessibleRole(AccessibleRole.TEXT);
    return box;
  }
}
