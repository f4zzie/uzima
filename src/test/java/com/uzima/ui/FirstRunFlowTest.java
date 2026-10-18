package com.uzima.ui;

import static com.uzima.ui.FlowTestSupport.waitFor;
import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import com.uzima.ui.shell.SampleDestinations;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** First run on an empty database: create the administrator and land in the shell. */
@ExtendWith(ApplicationExtension.class)
class FirstRunFlowTest {

  private UiTestBackend ui;
  private Stage stage;

  @org.junit.jupiter.api.BeforeAll
  static void seedSampleData() {
    SampleData.shared();
  }

  @Start
  void start(Stage stage) {
    this.stage = stage;
    ui = new UiTestBackend();
    AppFlow flow = new AppFlow(ui.backend, SampleDestinations.all());
    stage.setScene(UiTheme.apply(new Scene(flow.root(), 1366, 768)));
    stage.show();
    flow.start();
  }

  @AfterEach
  void close() {
    ui.close();
  }

  @Test
  void createsTheAdministratorAndOpensTheShell(FxRobot robot) {
    waitFor(robot, "#displayName");
    StageSnapshots.saveAtStandardSizes(stage, "first-run");

    robot.clickOn("#displayName").write("Amina Odhiambo");
    robot.clickOn("#username").write("amina");
    robot.clickOn("#password").write("borehole-water-2026");
    robot.clickOn("#confirm").write("borehole-water-2026");
    robot.clickOn("#create");

    Label name = waitFor(robot, ".user-name");
    assertThat(name.getText()).isEqualTo("Amina Odhiambo");
    assertThat(ui.services.auth().needsAdministrator()).isFalse();
  }

  @Test
  void shortPasswordIsReportedUnderThePasswordField(FxRobot robot) {
    waitFor(robot, "#displayName");
    robot.clickOn("#displayName").write("Amina Odhiambo");
    robot.clickOn("#username").write("amina");
    robot.clickOn("#password").write("short");
    robot.clickOn("#confirm").write("short");
    robot.clickOn("#create");

    FormField password = robot.lookup(".form-field").nth(2).query();
    waitUntil(password::hasError);
    assertThat(password.getLabel()).isEqualTo("Password");
    assertThat(password.getError()).contains("at least 10 characters");
    assertThat(password.errorNode().isVisible()).isTrue();
    assertThat(robot.lookup(".banner").queryAs(Banner.class).isShowing()).isFalse();
    assertThat(robot.lookup("#password").query().isFocused()).isTrue();
    assertThat(ui.services.auth().needsAdministrator()).isTrue();
  }

  @Test
  void allFieldErrorsShowAtOnce(FxRobot robot) {
    waitFor(robot, "#displayName");
    robot.clickOn("#username").write("a!");
    robot.clickOn("#password").write("short");
    robot.clickOn("#confirm").write("short");
    robot.clickOn("#create");

    FormField displayName = robot.lookup(".form-field").nth(0).query();
    FormField username = robot.lookup(".form-field").nth(1).query();
    waitUntil(username::hasError);
    assertThat(displayName.getError()).isEqualTo("Display name is required");
    assertThat(username.getError()).startsWith("Use 3–32");
    StageSnapshots.saveAtStandardSizes(stage, "first-run-errors");
  }

  @Test
  void mismatchedConfirmationIsCaughtLocally(FxRobot robot) {
    waitFor(robot, "#displayName");
    robot.clickOn("#displayName").write("Amina Odhiambo");
    robot.clickOn("#username").write("amina");
    robot.clickOn("#password").write("borehole-water-2026");
    robot.clickOn("#confirm").write("borehole-water-2025");
    robot.clickOn("#create");

    FormField confirm = robot.lookup(".form-field").nth(3).query();
    assertThat(confirm.getError()).isEqualTo("Passwords don't match");
    assertThat(ui.services.auth().needsAdministrator()).isTrue();
  }
}
