package com.uzima.ui.component;

import javafx.scene.control.TableColumn;

/**
 * How a {@link DataTable} column takes up width. Short, predictable values (IDs, categories,
 * counties, phones, statuses, dates, amounts) are fixed or bounded; names and descriptions flex and
 * absorb the remaining space.
 */
public sealed interface ColumnWidth {

  /** Always exactly {@code width}; the user can't resize it. */
  record Fixed(double width) implements ColumnWidth {}

  /** Starts at {@code min} and may grow up to {@code max}. */
  record Range(double min, double max) implements ColumnWidth {}

  /** At least {@code min}; takes whatever space is left over. */
  record Flex(double min) implements ColumnWidth {}

  static ColumnWidth fixed(double width) {
    return new Fixed(width);
  }

  static ColumnWidth range(double min, double max) {
    return new Range(min, max);
  }

  static ColumnWidth flex(double min) {
    return new Flex(min);
  }

  /** Applies this policy's min, preferred and max widths to {@code column}. */
  default void applyTo(TableColumn<?, ?> column) {
    switch (this) {
      case Fixed f -> {
        column.setMinWidth(f.width());
        column.setPrefWidth(f.width());
        column.setMaxWidth(f.width());
        column.setResizable(false);
      }
      case Range r -> {
        column.setMinWidth(r.min());
        column.setPrefWidth(r.min());
        column.setMaxWidth(r.max());
      }
      case Flex x -> {
        column.setMinWidth(x.min());
        column.setPrefWidth(x.min());
        column.setMaxWidth(Double.MAX_VALUE);
      }
    }
  }
}
