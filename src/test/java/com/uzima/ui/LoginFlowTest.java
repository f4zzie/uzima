package com.uzima.ui;

import static com.uzima.ui.FlowTestSupport.waitFor;
import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.user.Role;
import com.uzima.service.AuditAction;
import com.uzima.service.AuditQuery;
import com.uzima.service.LoginResult;
import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import com.uzima.ui.shell.SampleDestinations;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** Sign-in through the real screens, services and an in-memory database. */
@ExtendWith(ApplicationExtension.class)
class LoginFlowTest {

  private UiTestBackend ui;
  private Stage stage;

  @org.junit.jupiter.api.BeforeAll
  static void seedSampleData() {
    SampleData.shared();
  }

  @Start
  void start(Stage stage) {
    this.stage = stage;
    ui = new UiTestBackend().withAdministrator();
    AppFlow flow = new AppFlow(ui.backend, SampleDestinations.all());
    stage.setScene(UiTheme.apply(new Scene(flow.root(), 1366, 768)));
    stage.show();
    stage.toFront();
    stage.requestFocus();
    flow.start();
  }

  @AfterEach
  void close() {
    ui.close();
  }

  @Test
  void signingInOpensTheShellWithTheUsersName(FxRobot robot) {
    waitFor(robot, "#username");
    StageSnapshots.saveAtStandardSizes(stage, "login");

    robot.clickOn("#username").write(UiTestBackend.ADMIN_USERNAME);
    robot.clickOn("#password").write(UiTestBackend.ADMIN_PASSWORD);
    robot.clickOn("#signIn");

    Label name = waitFor(robot, ".user-name");
    assertThat(name.getText()).isEqualTo("Amina Odhiambo");
    assertThat(robot.lookup(".user-role").queryAs(Label.class).getText()).isEqualTo("Admin");
    assertThat(robot.lookup(".nav-item").queryAll()).hasSize(9);

    robot
        .press(KeyCode.CONTROL)
        .press(KeyCode.DIGIT2)
        .release(KeyCode.DIGIT2)
        .release(KeyCode.CONTROL);
    waitUntil(
        () -> "Clients".equals(robot.lookup(".top-bar-title").queryAs(Label.class).getText()));
    // A click (not the shortcut) last, so the snapshot shows the selected state without the
    // keyboard focus ring.
    robot.clickOn(robot.lookup(".nav-item").nth(1).queryAs(javafx.scene.Node.class));
    StageSnapshots.saveAtStandardSizes(stage, "shell");
  }

  @Test
  void theSidebarFocusRingAppearsOnlyForKeyboardFocus(FxRobot robot) {
    waitFor(robot, "#username");
    robot.clickOn("#username").write(UiTestBackend.ADMIN_USERNAME);
    robot.clickOn("#password").write(UiTestBackend.ADMIN_PASSWORD);
    robot.clickOn("#signIn");
    waitFor(robot, ".user-name");

    robot.clickOn("Jobs");
    javafx.scene.Node jobs = robot.lookup(".nav-item").nth(2).query();
    waitUntil(jobs::isFocused);
    assertThat(KeyboardFocusState.of(jobs)).isFalse();

    robot.type(KeyCode.DOWN);
    javafx.scene.Node payments = robot.lookup(".nav-item").nth(3).query();
    waitUntil(payments::isFocused);
    assertThat(KeyboardFocusState.of(payments)).isTrue();
    assertThat(KeyboardFocusState.of(jobs)).isFalse();
  }

  /** Reads the keyboard-focus state on the FX thread. */
  private static final class KeyboardFocusState {
    static boolean of(javafx.scene.Node node) {
      return FxTest.onFx(() -> com.uzima.ui.component.KeyboardFocus.hasKeyboardFocus(node));
    }
  }

  @Test
  void keyboardOnlySignIn(FxRobot robot) {
    waitFor(robot, "#username");
    // The username box has focus when the screen opens.
    waitUntil(() -> robot.lookup("#username").query().isFocused());
    robot.write(UiTestBackend.ADMIN_USERNAME);
    robot.type(KeyCode.TAB);
    robot.write(UiTestBackend.ADMIN_PASSWORD);
    robot.type(KeyCode.ENTER);

    assertThat(waitFor(robot, ".user-name").isVisible()).isTrue();
  }

  @Test
  void wrongPasswordShowsTheGenericMessage(FxRobot robot) {
    waitFor(robot, "#username");
    robot.clickOn("#username").write(UiTestBackend.ADMIN_USERNAME);
    robot.clickOn("#password").write("not-the-password");
    robot.type(KeyCode.ENTER);

    Banner banner = waitFor(robot, ".banner");
    assertThat(banner.getText()).isEqualTo(LoginResult.FAILURE_MESSAGE);
    assertThat(robot.lookup("#password").queryTextInputControl().getText()).isEmpty();
    StageSnapshots.saveAtStandardSizes(stage, "login-error");
  }

  @Test
  void blankFieldsAreFlaggedUnderTheFieldWithoutCallingTheService(FxRobot robot) {
    waitFor(robot, "#username");
    robot.clickOn("#signIn");

    FormField username = robot.lookup(".form-field").nth(0).query();
    FormField password = robot.lookup(".form-field").nth(1).query();
    assertThat(username.getError()).isEqualTo("Enter your username");
    assertThat(password.getError()).isEqualTo("Enter your password");
    assertThat(robot.lookup(".banner").queryAs(Banner.class).isShowing()).isFalse();
    ui.services
        .auth()
        .login(UiTestBackend.ADMIN_USERNAME, UiTestBackend.ADMIN_PASSWORD.toCharArray());
    assertThat(ui.services.audit().search(AuditQuery.latest(50)))
        .noneMatch(e -> e.action() == AuditAction.LOGIN_FAILED);
  }

  @Test
  void clerkSeesOnlyTheirPages(FxRobot robot) {
    ui.services
        .auth()
        .login(UiTestBackend.ADMIN_USERNAME, UiTestBackend.ADMIN_PASSWORD.toCharArray());
    var clerk = ui.services.users().create("otieno", "Otieno Ochieng", Role.CLERK);
    ui.services.auth().signOut();
    waitFor(robot, "#username");

    robot.clickOn("#username").write("otieno");
    robot.clickOn("#password").write(clerk.temporaryPassword());
    robot.type(KeyCode.ENTER);

    waitFor(robot, ".user-name");
    assertThat(robot.lookup(".nav-item").queryAll()).hasSize(4);
    StageSnapshots.saveAtStandardSizes(stage, "shell-clerk");
  }
}
