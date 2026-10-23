package com.uzima.ui.component;

import com.uzima.domain.money.Money;

/** An amount in Kenyan Shillings with a leading "KSh" label, shown as {@code 1,234,567.00}. */
public class MoneyField extends NumericField<Money> {

  private boolean allowZero;

  public MoneyField() {
    super("KSh", true);
    getStyleClass().add("money-field");
    setPromptText("0.00");
  }

  /** Whether 0.00 is an acceptable value. Off by default: most amounts must be positive. */
  public boolean isAllowZero() {
    return allowZero;
  }

  public void setAllowZero(boolean allowZero) {
    this.allowZero = allowZero;
    revalidate();
  }

  @Override
  protected Money parse(String plain) {
    return Money.parse(plain, "amount");
  }

  @Override
  protected String check(Money money) {
    return !allowZero && money.isZero() ? "Enter an amount greater than zero" : null;
  }

  @Override
  protected String format(Money money) {
    return money.formatPlain();
  }

  @Override
  protected String blankMessage() {
    return "Enter an amount";
  }
}