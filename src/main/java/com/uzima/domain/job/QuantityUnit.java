package com.uzima.domain.job;

/** Unit of a quote line's quantity. */
public enum QuantityUnit {
  EACH(""),
  METRE("m");

  private final String symbol;

  QuantityUnit(String symbol) {
    this.symbol = symbol;
  }

  /** Short suffix for display, e.g. "m"; empty for {@link #EACH}. */
  public String symbol() {
    return symbol;
  }
}
