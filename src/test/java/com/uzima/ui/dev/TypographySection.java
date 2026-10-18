package com.uzima.ui.dev;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/** The type scale, set in Inter. */
final class TypographySection implements StyleGuideSection {

  private static final String[][] STYLES = {
    {"text-display", "Display · 30 SemiBold", "Uzima Borehole Drilling"},
    {"text-headline", "Headline · 24 SemiBold", "JOB-2026-00042"},
    {"text-title", "Title · 20 SemiBold", "Quote breakdown"},
    {"text-subtitle", "Subtitle · 16 SemiBold", "Client details"},
    {"text-body", "Body · 14 Regular", "Drilling 150 m in Machakos for a domestic client."},
    {"text-small", "Small · 13 Regular", "Last updated 18/10/2026 09:15"},
    {"text-caption", "Caption · 12 muted", "Prices include VAT at 16%"},
    {"text-overline", "Overline · 11 SemiBold", "AMOUNT DUE"},
    {"text-numeric", "Numeric · 14 Medium", "KSh 684,400.00"},
  };

  @Override
  public String title() {
    return "Typography";
  }

  @Override
  public String description() {
    return "Inter (OFL), bundled with the app. Regular for text, Medium for numbers and labels,"
        + " SemiBold for headings.";
  }

  @Override
  public Node build() {
    GridPane grid = new GridPane();
    grid.getStyleClass().add("style-guide-grid");
    for (int row = 0; row < STYLES.length; row++) {
      Label name = new Label(STYLES[row][1]);
      name.getStyleClass().add("text-caption");
      Label sample = new Label(STYLES[row][2]);
      sample.getStyleClass().add(STYLES[row][0]);
      grid.addRow(row, name, sample);
    }
    return grid;
  }
}
