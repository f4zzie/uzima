package com.uzima.ui.shell;

import javafx.scene.shape.SVGPath;

/**
 * Navigation glyphs drawn for Uzima on a 20×20 grid: simple strokes-as-shapes so they stay crisp at
 * small sizes. Colour comes from CSS ({@code .nav-icon}).
 */
public enum Icon {
  /** Four tiles. */
  DASHBOARD("M2 2h7v9H2z M11 2h7v5h-7z M11 9h7v9h-7z M2 13h7v5H2z"),
  /** Two heads and shoulders. */
  CLIENTS(
      "M7 3a3 3 0 1 1 0 6a3 3 0 1 1 0-6z M1 17c0-3.3 2.7-6 6-6s6 2.7 6 6z"
          + " M14 4a2.5 2.5 0 1 1 0 5a2.5 2.5 0 1 1 0-5z M14.5 11c2.5 0.3 4.5 2.8 4.5 6h-4.6"
          + "c0-2.3-0.8-4.3-2.1-5.6c0.7-0.3 1.4-0.4 2.2-0.4z"),
  /** A drilling rig: a derrick over the ground with the drill string going down. */
  JOBS("M10 1L3.5 15h2.2L10 5.6l4.3 9.4h2.2z M9 7h2v12H9z M1 15h6v2H1z M13 15h6v2h-6z"),
  /** A banknote. */
  PAYMENTS("M1 5h18v10H1z M3 7v6h14V7z M10 8a2 2 0 1 1 0 4a2 2 0 1 1 0-4z"),
  /** Rising bars. */
  REPORTS("M2 17h16v2H2z M3 10h3v6H3z M8.5 6h3v10h-3z M14 2h3v14h-3z"),
  /** A price tag. */
  CATALOG("M2 2h8l8 8l-8 8l-8-8z M6 5a1.5 1.5 0 1 0 0 3a1.5 1.5 0 1 0 0-3z"),
  /** A single person with a badge line. */
  USERS("M10 2a4 4 0 1 1 0 8a4 4 0 1 1 0-8z M2 18c0-4 3.6-7 8-7s8 3 8 7z"),
  /** A gear approximated by a ring with four teeth. */
  SETTINGS(
      "M8.5 1h3v3h-3z M8.5 16h3v3h-3z M1 8.5h3v3H1z M16 8.5h3v3h-3z"
          + " M10 4a6 6 0 1 1 0 12a6 6 0 1 1 0-12z M10 7.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5z"),
  /** A clipboard with lines: the audit log. */
  AUDIT("M4 2h12v17H4z M6 6h8v1.5H6z M6 9.5h8V11H6z M6 13h5v1.5H6z");

  private final String path;

  Icon(String path) {
    this.path = path;
  }

  public String path() {
    return path;
  }

  /** A fresh node for this glyph, styled by {@code .nav-icon}. */
  public SVGPath node() {
    SVGPath svg = new SVGPath();
    svg.setContent(path);
    svg.getStyleClass().add("nav-icon");
    svg.setFillRule(javafx.scene.shape.FillRule.EVEN_ODD);
    return svg;
  }
}
