package com.uzima.ui.component;

import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The line under the top bar on a page: a subtitle, an optional count and the page's actions (at
 * most one primary). The page's name is already in the top bar, so it is never repeated here.
 */
public class PageHeader extends HBox {

  private final Label subtitle = new Label();
  private final Label count = new Label();
  private final HBox actions = new HBox();

  public PageHeader(String subtitleText) {
    getStyleClass().add("page-header");
    subtitle.getStyleClass().add("page-subtitle");
    subtitle.setText(subtitleText);
    subtitle.setWrapText(true);
    subtitle.setMinHeight(Region.USE_PREF_SIZE);
    count.getStyleClass().addAll("page-count", "figures");
    count.visibleProperty().bind(count.textProperty().isNotEmpty());
    count.managedProperty().bind(count.visibleProperty());
    actions.getStyleClass().add("page-actions");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    VBox text = new VBox(2, subtitle, count);
    getChildren().addAll(text, spacer, actions);
  }

  public void setSubtitle(String text) {
    subtitle.setText(text);
  }

  public String getSubtitle() {
    return subtitle.getText();
  }

  /** Shows {@code text} as the count, e.g. a {@link DataTable#countTextProperty()}. */
  public void bindCount(ObservableStringValue text) {
    count.textProperty().bind(text);
  }

  public void setCount(String text) {
    count.textProperty().unbind();
    count.setText(text == null ? "" : text);
  }

  public String getCount() {
    return count.getText();
  }

  public ObservableList<Node> getActions() {
    return actions.getChildren();
  }

  /** A header above a table: binds the table's count here and hides it from the toolbar. */
  public static PageHeader forTable(String subtitleText, DataTable<?> table) {
    PageHeader header = new PageHeader(subtitleText);
    table.setShowCount(false);
    header.bindCount(table.countTextProperty());
    return header;
  }
}
