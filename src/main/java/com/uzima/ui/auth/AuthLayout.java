package com.uzima.ui.auth;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

/** The two-pane frame shared by the sign-in and first-run screens: brand panel and form. */
final class AuthLayout {

  /** A water drop on a 24×32 grid. */
  private static final String DROP = "M12 0C12 0 0 14 0 21a12 11 0 0 0 24 0C24 14 12 0 12 0z";

  private static final String[] POINTS = {
    "Quotes priced from your own catalog, VAT included",
    "Every job tracked from survey to completion",
    "Payments and receipts that always reconcile",
  };

  private AuthLayout() {}

  static Region wrap(Node form) {
    StackPane formSide = new StackPane(form);
    formSide.getStyleClass().add("auth-form-side");
    // Centred when it fits; scrolls on short windows instead of clipping the submit button.
    ScrollPane scroll = new ScrollPane(formSide);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("auth-scroll");
    formSide.minHeightProperty().bind(scroll.viewportBoundsProperty().map(Bounds::getHeight));
    HBox.setHgrow(scroll, Priority.ALWAYS);
    HBox layout = new HBox(brandPanel(), scroll);
    layout.getStyleClass().add("auth-screen");
    return layout;
  }

  private static Region grow() {
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    return spacer;
  }

  private static Region brandPanel() {
    SVGPath drop = new SVGPath();
    drop.setContent(DROP);
    drop.getStyleClass().add("auth-brand-mark");
    Label wordmark = new Label("Uzima");
    wordmark.getStyleClass().add("auth-wordmark");
    HBox brand = new HBox(drop, wordmark);
    brand.getStyleClass().add("auth-brand");

    Label tagline = new Label("Borehole drilling management");
    tagline.getStyleClass().add("auth-tagline");
    tagline.setWrapText(true);

    VBox points = new VBox();
    points.getStyleClass().add("auth-points");
    for (String text : POINTS) {
      Region tick = new Region();
      tick.getStyleClass().add("auth-point-mark");
      Label label = new Label(text);
      label.getStyleClass().add("auth-point");
      label.setWrapText(true);
      HBox row = new HBox(tick, label);
      row.getStyleClass().add("auth-point-row");
      points.getChildren().add(row);
    }

    Label footer = new Label("Uzima · desktop edition");
    footer.getStyleClass().add("auth-footer");

    VBox panel = new VBox(grow(), brand, tagline, points, grow(), footer);
    panel.getStyleClass().add("auth-brand-panel");
    panel.setAlignment(Pos.TOP_LEFT);
    return panel;
  }
}
