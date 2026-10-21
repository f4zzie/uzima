package com.uzima.ui.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The centred message shown where content would be: nothing yet, no search results, loading, or an
 * error with a retry. Used as a table placeholder and on its own.
 */
public class EmptyState extends VBox {

  private final Label title = new Label();
  private final Label text = new Label();
  private final Button retry = new Button("Try again");

  public EmptyState() {
    getStyleClass().add("empty-state");
    setAlignment(Pos.CENTER);
    title.getStyleClass().add("empty-state-title");
    text.getStyleClass().add("empty-state-text");
    text.setWrapText(true);
    text.setMaxWidth(420);
    text.setMinHeight(Region.USE_PREF_SIZE);
    title.setMinHeight(Region.USE_PREF_SIZE);
  }

  /** Title, explanation and an optional action such as a "New client" button. */
  public void showMessage(String titleText, String body, Node action) {
    setKind("empty");
    show(titleText, body);
    if (action != null) {
      getChildren().add(action);
    }
  }

  /** "No results for 'x'" with a link that clears the search. */
  public void showNoMatches(String query, Runnable clear) {
    setKind("no-matches");
    show("No results for “" + query + "”", "Check the spelling or try fewer words.");
    Hyperlink link = new Hyperlink("Clear search");
    link.setOnAction(e -> clear.run());
    getChildren().add(link);
  }

  public void showLoading() {
    showLoading("Loading…");
  }

  /** A spinner with {@code message}, e.g. "Loading clients…". */
  public void showLoading(String message) {
    setKind("loading");
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().add("empty-state-spinner");
    title.setText(message);
    text.setText("");
    getChildren().setAll(spinner, title);
    setAccessibleText(message);
  }

  /** The message and, when {@code retry} is set, a Retry button. */
  public void showError(String message, Runnable retry) {
    setKind("error");
    show("Something went wrong", message);
    this.retry.setOnAction(retry == null ? null : e -> retry.run());
    this.retry.setVisible(retry != null);
    if (retry != null) {
      getChildren().add(this.retry);
    }
  }

  /** The "Try again" button shown by {@link #showError}. */
  public Button retryButton() {
    return retry;
  }

  public String getTitle() {
    return title.getText();
  }

  public String getText() {
    return text.getText();
  }

  private void show(String titleText, String body) {
    title.setText(titleText);
    text.setText(body == null ? "" : body);
    getChildren().setAll(title);
    if (body != null && !body.isEmpty()) {
      getChildren().add(text);
    }
    setAccessibleText(titleText + ". " + text.getText());
  }

  private void setKind(String kind) {
    getStyleClass().removeIf(c -> c.startsWith("empty-state-kind-"));
    getStyleClass().add("empty-state-kind-" + kind);
  }
}
