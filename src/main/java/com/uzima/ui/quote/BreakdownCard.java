package com.uzima.ui.quote;

import javafx.scene.control.ScrollPane;

/** The breakdown panel as a fixed-width card beside the form, scrolling on its own if long. */
final class BreakdownCard extends ScrollPane {

  BreakdownCard(QuoteEditorViewModel model) {
    super(new BreakdownPanel(model));
    getStyleClass().add("breakdown-card");
    setFitToWidth(true);
    setHbarPolicy(ScrollBarPolicy.NEVER);
    setFocusTraversable(false);
  }
}
