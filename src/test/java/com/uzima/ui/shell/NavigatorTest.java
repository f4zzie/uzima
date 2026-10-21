package com.uzima.ui.shell;

import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.AppFlow;
import com.uzima.ui.FxTest;
import com.uzima.ui.SampleData;
import com.uzima.ui.ShellHarness;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** Detail pages stack on top of a sidebar page, with a trail and Back. */
@ExtendWith(ApplicationExtension.class)
class NavigatorTest {

  private AppFlow flow;

  @BeforeAll
  static void seed() {
    SampleData.shared();
  }

  @Start
  void start(Stage stage) {
    flow =
        ShellHarness.show(
            stage, SampleData.shared().ui.backend, "clients", nav -> new Label("Client list"));
  }

  @Test
  void pushShowsTheTrailAndBackReturns(FxRobot robot) {
    Navigator nav = flow.navigator().orElseThrow();
    assertThat(text(robot, ".top-bar-title")).isEqualTo("Clients");
    assertThat(robot.lookup(".top-bar-back").query().isVisible()).isFalse();

    FxTest.onFx(() -> nav.push("Wanjiru Kamau", new Label("Client detail")));
    FxTest.onFx(() -> nav.push("JOB-2026-00002", new Label("Job detail")));
    assertThat(text(robot, ".top-bar-title")).isEqualTo("JOB-2026-00002");
    assertThat(text(robot, ".top-bar-trail")).isEqualTo("Clients  ›  Wanjiru Kamau  ›");
    assertThat(robot.lookup(".top-bar-back").query().isVisible()).isTrue();

    robot.clickOn(".top-bar-back");
    waitUntil(() -> "Wanjiru Kamau".equals(text(robot, ".top-bar-title")));

    robot.press(KeyCode.ALT, KeyCode.LEFT).release(KeyCode.LEFT, KeyCode.ALT);
    waitUntil(() -> "Clients".equals(text(robot, ".top-bar-title")));
    assertThat(robot.lookup("Client list").tryQuery()).isPresent();
  }

  @Test
  void replaceSwapsTheDetailPage(FxRobot robot) {
    Navigator nav = flow.navigator().orElseThrow();
    FxTest.onFx(() -> nav.push("New job", new Label("Editor")));
    FxTest.onFx(() -> nav.replace("JOB-2026-00007", new Label("Saved job")));
    assertThat(text(robot, ".top-bar-trail")).isEqualTo("Clients  ›");
    FxTest.onFx(nav::back);
    assertThat(text(robot, ".top-bar-title")).isEqualTo("Clients");
  }

  @Test
  void openSwitchesSidebarPagesAndClearsTheStack(FxRobot robot) {
    Navigator nav = flow.navigator().orElseThrow();
    FxTest.onFx(() -> nav.push("Wanjiru Kamau", new Label("Client detail")));
    assertThat(FxTest.onFx(() -> nav.open("jobs"))).isTrue();
    assertThat(text(robot, ".top-bar-title")).isEqualTo("Jobs");
    assertThat(FxTest.onFx(() -> nav.open("no-such-page"))).isFalse();
  }

  private static String text(FxRobot robot, String query) {
    return FxTest.onFx(() -> robot.lookup(query).queryAs(Label.class).getText());
  }
}
