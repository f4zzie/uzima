package com.uzima.ui.component;

import static com.uzima.ui.component.MoneyFieldTest.robotFx;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.common.Metres;
import com.uzima.ui.KeyboardStageExtension;
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

@ExtendWith({ApplicationExtension.class, KeyboardStageExtension.class})
class MetresFieldTest {

  private MetresField field;
  private TextField other;

  @Start
  void start(Stage stage) {
    field = new MetresField();
    other = new TextField();
    stage.setScene(UiTheme.apply(new Scene(new VBox(field, other), 400, 200)));
    stage.show();
    stage.toFront();
    stage.requestFocus();
  }

  @Test
  void typedLengthBecomesMetresAndIsTidiedOnBlur(FxRobot robot) {
    robot.clickOn(field.textField()).write("1250.5");
    assertThat(field.getValue()).isEqualTo(Metres.of("1250.50"));
    robot.clickOn(other);
    assertThat(field.textField().getText()).isEqualTo("1,250.50");
  }

  @Test
  void zeroIsAValidLength() {
    robotFx(() -> field.textField().setText("0"));
    assertThat(field.getValue()).isEqualTo(Metres.ZERO);
  }

  @Test
  void blankAndTooLongAreProblems() {
    robotFx(() -> field.textField().setText(""));
    assertThat(field.getProblem()).isEqualTo("Enter a number of metres");
    robotFx(() -> field.textField().setText("1000000"));
    assertThat(field.getValue()).isNull();
    assertThat(field.getProblem()).contains("999,999.99");
  }

  @Test
  void unitIsShownAfterTheNumber() {
    assertThat(field.getChildren().get(1).getStyleClass()).contains("trailing");
    robotFx(() -> field.setValue(Metres.of(150)));
    assertThat(field.textField().getText()).isEqualTo("150.00");
  }
}
