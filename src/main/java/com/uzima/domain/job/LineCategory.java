package com.uzima.domain.job;

/** Revenue category of a quote line; drives the revenue-by-service report. */
public enum LineCategory {
  DRILLING("Drilling"),
  PUMP("Pump"),
  TANK("Tank"),
  PLUMBING("Plumbing"),
  SURVEY_FEE("Survey fee"),
  AUTHORITY_FEE("Authority fee");

  private final String label;

  LineCategory(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }
}
