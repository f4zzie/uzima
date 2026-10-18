package com.uzima.ui.component;

import com.uzima.domain.user.PasswordPolicy;
import com.uzima.domain.user.PasswordPolicy.Strength;
import java.util.Locale;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Three bars and a word (weak, fair, strong) that follow what is typed into a password field. The
 * rating is {@link PasswordPolicy#strengthOf}, a hint only: it never blocks a valid password.
 */
public class PasswordStrengthHint extends HBox {

  private final Region[] bars = {new Region(), new Region(), new Region()};
  private final Label label = new Label();
  private Strength strength = Strength.TOO_SHORT;

  public PasswordStrengthHint(ObservableStringValue password) {
    getStyleClass().add("password-strength");
    HBox barRow = new HBox();
    barRow.getStyleClass().add("password-strength-bars");
    for (Region bar : bars) {
      bar.getStyleClass().add("password-strength-bar");
      barRow.getChildren().add(bar);
    }
    label.getStyleClass().add("password-strength-label");
    getChildren().addAll(barRow, label);
    update(password.get());
    password.addListener((obs, old, now) -> update(now));
  }

  public Strength getStrength() {
    return strength;
  }

  public String getText() {
    return label.getText();
  }

  private void update(String password) {
    strength = PasswordPolicy.strengthOf(password == null ? "" : password);
    int lit =
        switch (strength) {
          case TOO_SHORT -> 0;
          case WEAK -> 1;
          case FAIR -> 2;
          case STRONG -> 3;
        };
    for (int i = 0; i < bars.length; i++) {
      bars[i].getStyleClass().remove("on");
      if (i < lit) {
        bars[i].getStyleClass().add("on");
      }
    }
    getStyleClass().removeAll("weak", "fair", "strong");
    if (strength != Strength.TOO_SHORT) {
      getStyleClass().add(strength.name().toLowerCase(Locale.ROOT));
    }
    label.setText(
        switch (strength) {
          case TOO_SHORT -> "Too short";
          case WEAK -> "Weak: longer, or mix letters, numbers and symbols";
          case FAIR -> "Fair";
          case STRONG -> "Strong";
        });
    setAccessibleText("Password strength: " + label.getText());
  }
}
