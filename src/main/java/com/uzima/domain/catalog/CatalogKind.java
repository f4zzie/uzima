package com.uzima.domain.catalog;

/** The four kinds of priced catalog item. */
public enum CatalogKind {
  DRILLING("Drilling type", "Down payment"),
  PUMP("Pump type", "Base cost"),
  TANK("Tank", "Price"),
  PIPE("Pipe type", "Cost per metre");

  private final String label;
  private final String priceLabel;

  CatalogKind(String label, String priceLabel) {
    this.label = label;
    this.priceLabel = priceLabel;
  }

  public String label() {
    return label;
  }

  /** What {@link CatalogItem#price()} means for this kind. */
  public String priceLabel() {
    return priceLabel;
  }

  public static CatalogKind of(CatalogItem item) {
    return switch (item) {
      case DrillingType d -> DRILLING;
      case PumpType p -> PUMP;
      case TankType t -> TANK;
      case PipeType p -> PIPE;
    };
  }
}
