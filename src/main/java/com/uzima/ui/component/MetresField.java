package com.uzima.ui.component;

import com.uzima.domain.common.Metres;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** A length in metres with a trailing "m" label, shown as {@code 1,250.50}. */
public class MetresField extends NumericField<Metres> {

  private boolean allowZero = true;

  public MetresField() {
    super("m", false);
    getStyleClass().add("metres-field");
    setPromptText("0.00");
  }

  @Override
  protected Metres parse(String plain) {
    return Metres.of(plain);
  }

  public boolean isAllowZero() {
    return allowZero;
  }

  /** When false, 0 m is rejected ("Enter more than 0 m"), e.g. for a borehole depth. */
  public void setAllowZero(boolean allowZero) {
    this.allowZero = allowZero;
    revalidate();
  }

  @Override
  protected String check(Metres metres) {
    return !allowZero && metres.isZero() ? "Enter more than 0 m" : null;
  }

  @Override
  protected String format(Metres metres) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator(',');
    symbols.setDecimalSeparator('.');
    return new DecimalFormat("#,##0.00", symbols).format(metres.value());
  }

  @Override
  protected String blankMessage() {
    return "Enter a number of metres";
  }
}
