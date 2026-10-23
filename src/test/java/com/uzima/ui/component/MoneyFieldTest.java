package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.money.Money;
import com.uzima.ui.UiTheme;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class MoneyFieldTest {

  private MoneyField field;
  private TextField other;

  @Start
  void start(Stage stage) {
    field = new MoneyField();
    other = new TextField();
    stage.setScene(UiTheme.apply(new Scene(new VBox(field, other), 400, 200)));
    stage.show();
    stage.toFront();
    stage.requestFocus();
  }

  @Test
  void typingAnAmountSetsTheValue(FxRobot robot) {
    robot.clickOn(field.textField()).write("1,234.5");
    assertThat(field.getValue()).isEqualTo(Money.of("1234.50"));
    assertThat(field.getProblem()).isNull();
  }

  @Test
  void rejectsCharactersThatCannotBeAnAmount(FxRobot robot) {
    robot.clickOn(field.textField()).write("12a.3.456-");
    assertThat(field.textField().getText()).isEqualTo("12.34");
  }

  @Test
  void reformatsWhenFocusLeaves(FxRobot robot) {
    robot.clickOn(field.textField()).write("1234567.5");
    robot.clickOn(other);
    assertThat(field.textField().getText()).isEqualTo("1,234,567.50");
  }

  @Test
  void blankHasNoValueAndAProblem() {
    set("");
    assertThat(field.getValue()).isNull();
    assertThat(field.getProblem()).isEqualTo("Enter an amount");
  }

  @Test
  void zeroIsRejectedUnlessAllowed() {
    set("0");
    assertThat(field.getValue()).isNull();
    assertThat(field.getProblem()).isEqualTo("Enter an amount greater than zero");

    robotFx(() -> field.setAllowZero(true));
    assertThat(field.isAllowZero()).isTrue();
    assertThat(field.getValue()).isEqualTo(Money.ZERO);
    assertThat(field.getProblem()).isNull();
  }

  @Test
  void settingTheValueShowsItFormatted() {
    robotFx(() -> field.setValue(Money.of("684400")));
    assertThat(field.textField().getText()).isEqualTo("684,400.00");
    robotFx(() -> field.setValue(null));
    assertThat(field.textField().getText()).isEmpty();
  }

  @Test
  void requestingFocusFocusesTheTextField() {
    robotFx(field::requestFocus);
    assertThat(field.textField().isFocused()).isTrue();
  }

  @Test
  void showsTheCurrencyBeforeTheAmount() {
    assertThat(field.getChildren().get(0).getStyleClass()).contains("input-adornment", "leading");
    assertThat(field.textField().getStyleClass()).contains("numeric", "has-leading");
  }

  private void set(String text) {
    robotFx(() -> field.textField().setText(text));
  }

  static void robotFx(Runnable r) {
    WaitForAsyncUtils.asyncFx(r);
    WaitForAsyncUtils.waitForFxEvents();
  }
}
