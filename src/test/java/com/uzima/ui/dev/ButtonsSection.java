package com.uzima.ui.dev;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Button variants: primary, secondary (the default), danger and ghost, in three sizes. */
final class ButtonsSection implements StyleGuideSection {

  @Override
  public String title() {
    return "Buttons";
  }

  @Override
  public String description() {
    return "One primary action per view. Secondary is the default. Danger only for cancelling jobs,"
        + " reversals and deactivation.";
  }

  @Override
  public Node build() {
    Button disabled = button("Disabled", "primary");
    disabled.setDisable(true);
    return new VBox(
        16,
        row(
            "Variants",
            button("Save quote", "primary"),
            button("Cancel"),
            button("Cancel job", "danger"),
            button("View history", "ghost"),
            new Hyperlink("Forgot password?")),
        row(
            "Sizes",
            button("Small", "primary", "small"),
            button("Default", "primary"),
            button("Large", "primary", "large")),
        row("States", disabled, button("Secondary small", "small")));
  }

  static Button button(String text, String... classes) {
    Button b = new Button(text);
    b.getStyleClass().addAll(classes);
    return b;
  }

  static HBox row(String caption, Node... nodes) {
    Label label = new Label(caption);
    label.getStyleClass().add("text-caption");
    label.setMinWidth(96);
    HBox row = new HBox(label);
    row.getChildren().addAll(nodes);
    row.getStyleClass().add("style-guide-row");
    return row;
  }
}
