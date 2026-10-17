package com.uzima.domain.payment;

/** Derived from a job's grand total and its payments; never stored. */
public enum PaymentStatus {
  NOT_PAID("Not paid"),
  PARTIALLY_PAID("Partially paid"),
  PAID("Paid");

  private final String label;

  PaymentStatus(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
