package com.uzima.ui.dev;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Every colour token, shown as a chip with its name. */
final class ColoursSection implements StyleGuideSection {

  private static final List<String> TOKENS =
      List.of(
          "primary",
          "primary-hover",
          "primary-soft",
          "accent",
          "accent-soft",
          "bg",
          "surface",
          "surface-alt",
          "surface-sunken",
          "border",
          "border-strong",
          "text",
          "text-muted",
          "text-subtle",
          "success",
          "success-soft",
          "warning",
          "warning-soft",
          "danger",
          "danger-soft",
          "info",
          "info-soft",
          "focus",
          "sidebar-bg",
          "sidebar-active");

  @Override
  public String title() {
    return "Colour tokens";
  }

  @Override
  public String description() {
    return "Looked-up colours on .root in theme.css. Everything else refers to these names.";
  }

  @Override
  public Node build() {
    FlowPane grid = new FlowPane(12, 16);
    for (String token : TOKENS) {
      Region chip = new Region();
      chip.getStyleClass().addAll("style-guide-chip", "style-guide-chip-" + token);
      Label name = new Label("-uz-" + token);
      name.getStyleClass().add("text-caption");
      VBox swatch = new VBox(chip, name);
      swatch.getStyleClass().add("style-guide-swatch");
      grid.getChildren().add(swatch);
    }
    grid.setPadding(Insets.EMPTY);
    return grid;
  }
}
