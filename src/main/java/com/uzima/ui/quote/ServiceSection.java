package com.uzima.ui.quote;

import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.Quote;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * One service in the quote form (drilling, pump, tank or plumbing): a header with an on/off check
 * box and the service's price from the live quote, and its inputs, shown only while it is on.
 */
final class ServiceSection extends VBox {

  private final CheckBox toggle;

  ServiceSection(
      String title,
      String hint,
      BooleanProperty on,
      LineCategory category,
      ObjectProperty<Quote> quote,
      Node body) {
    getStyleClass().add("service-section");
    toggle = new CheckBox(title);
    toggle.getStyleClass().add("service-toggle");
    toggle.selectedProperty().bindBidirectional(on);
    toggle.setAccessibleHelp(hint);
    Label hintLabel = new Label(hint);
    hintLabel.getStyleClass().add("text-caption");
    Label total = new Label();
    total.getStyleClass().addAll("text-numeric", "service-total");
    total
        .textProperty()
        .bind(
            quote
                .map(
                    q ->
                        on.get() && !q.totalFor(category).isZero()
                            ? "KSh " + q.totalFor(category).formatPlain()
                            : "")
                .orElse(""));
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(12, new VBox(2, toggle, hintLabel), spacer, total);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getStyleClass().add("service-header");
    body.getStyleClass().add("service-body");
    body.visibleProperty().bind(on);
    body.managedProperty().bind(on);
    getChildren().addAll(header, body);
    pseudoClassStateChanged(ON, on.get());
    on.addListener((obs, was, now) -> pseudoClassStateChanged(ON, now));
  }

  private static final javafx.css.PseudoClass ON = javafx.css.PseudoClass.getPseudoClass("on");

  CheckBox toggle() {
    return toggle;
  }
}
