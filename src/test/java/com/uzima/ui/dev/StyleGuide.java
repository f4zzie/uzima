package com.uzima.ui.dev;

import java.util.List;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Dev-only page showing the theme and every reusable component. It lives in test sources so it
 * never ships in the app or appears in its menu; StyleGuideSnapshotTest renders it to PNG.
 */
public final class StyleGuide {

  /** Sections in display order. Add new components' sections here. */
  static final List<StyleGuideSection> SECTIONS =
      List.of(
          new ColoursSection(), new TypographySection(), new ButtonsSection(), new FieldsSection());

  private StyleGuide() {}

  /** The whole guide as one tall column (for full-length snapshots). */
  public static VBox content() {
    VBox page = new VBox();
    page.getStyleClass().addAll("page");
    Label title = new Label("Uzima design system");
    title.getStyleClass().add("text-display");
    Label subtitle =
        new Label("Tokens and components. Dev only: not part of the app and not in its menu.");
    subtitle.getStyleClass().add("text-muted");
    page.getChildren().add(new VBox(4, title, subtitle));
    for (StyleGuideSection section : SECTIONS) {
      page.getChildren().add(card(section));
    }
    return page;
  }

  /** The guide in a scroll pane, as it would be viewed in a window. */
  public static Parent scrollable() {
    ScrollPane scroll = new ScrollPane(content());
    scroll.setFitToWidth(true);
    return scroll;
  }

  private static VBox card(StyleGuideSection section) {
    Label title = new Label(section.title());
    title.getStyleClass().add("text-title");
    Label description = new Label(section.description());
    description.getStyleClass().add("text-muted");
    description.setWrapText(true);
    VBox card = new VBox(new VBox(4, title, description), section.build());
    card.getStyleClass().addAll("card", "stack-16");
    return card;
  }
}
