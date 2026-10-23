package com.uzima.ui.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.AppFlow;
import com.uzima.ui.FxTest;
import com.uzima.ui.KeyboardStageExtension;
import com.uzima.ui.SampleData;
import com.uzima.ui.UiTheme;
import com.uzima.ui.shell.Destination;
import com.uzima.ui.shell.InitialFocus;
import com.uzima.ui.shell.SampleDestinations;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** Every available sidebar route, using real Clients and the remaining preview destinations. */
@ExtendWith({ApplicationExtension.class, KeyboardStageExtension.class})
class NavigationFocusTest {
  private final Map<String, TrackedFocus> pages = new HashMap<>();
  private Stage stage;

  @BeforeAll
  static void seed() {
    SampleData.shared();
  }

  @Start
  void start(Stage stage) {
    this.stage = stage;
    var backend = SampleData.shared().ui.backend;
    var destinations =
        SampleDestinations.all().stream()
            .map(
                d ->
                    new Destination(
                        d.id(),
                        d.title(),
                        d.icon(),
                        d.permission(),
                        nav -> {
                          Node root =
                              d.id().equals("clients")
                                  ? new ClientsPage(
                                          backend, nav, new ClientsTestPages(backend).attach(nav))
                                      .root()
                                  : d.view().apply(nav);
                          TrackedFocus tracked =
                              new TrackedFocus(InitialFocus.of(root).orElseThrow());
                          pages.put(d.id(), tracked);
                          return InitialFocus.attach(root, tracked);
                        }))
            .toList();
    AppFlow flow = new AppFlow(backend, destinations);
    stage.setScene(UiTheme.apply(new Scene(flow.root(), 1366, 768)));
    FxTest.traceFocus(stage.getScene());
    stage.show();
    flow.showShell(backend.session().requireUser());
  }

  @AfterEach
  void hide() {
    FxTest.onFx(stage::hide);
  }

  static Stream<Arguments> routes() {
    var all = SampleDestinations.all();
    return IntStream.range(0, all.size())
        .boxed()
        .flatMap(
            i ->
                Stream.of(
                    Arguments.of(i, all.get(i).id(), "mouse"),
                    Arguments.of(i, all.get(i).id(), "shortcut")));
  }

  @ParameterizedTest(name = "{1} via {2}")
  @MethodSource("routes")
  void navigationFinishesInTheDeclaredControl(int index, String id, String input, FxRobot robot) {
    // Exercise a real page swap, including when the target is the initial Dashboard route.
    robot.clickOn("#nav-" + (id.equals("settings") ? "dashboard" : "settings"));
    navigate(index, id, input, robot);
    assertInitialFocus(id);

    // Opening an already-selected route must also leave the sidebar.
    navigate(index, id, input, robot);
    assertInitialFocus(id);

    // Once navigation completes, later pulses must not take focus back from the user.
    ToggleButton nav = robot.lookup("#nav-" + id).queryAs(ToggleButton.class);
    FxTest.onFx(nav::requestFocus);
    FxTest.settle();
    FxTest.waitForFocus(stage, nav);
    assertThat(FxTest.onFx(() -> pages.get(id).requests)).isEqualTo(1);
  }

  private void navigate(int index, String id, String input, FxRobot robot) {
    FxTest.onFx(() -> FxTest.markFocus(stage.getScene(), "sending " + input + " to " + id));
    if (input.equals("mouse")) {
      robot.clickOn("#nav-" + id);
    } else {
      KeyCode digit = KeyCode.valueOf("DIGIT" + (index + 1));
      robot.press(KeyCode.CONTROL, digit).release(digit, KeyCode.CONTROL);
    }
  }

  private void assertInitialFocus(String id) {
    com.uzima.ui.FlowTestSupport.waitUntil(
        () -> FxTest.onFx(() -> pages.get(id).delegate.initialFocusTarget() != null));
    Node expected = FxTest.onFx(() -> pages.get(id).delegate.initialFocusTarget());
    FxTest.waitForFocus(stage, expected);
    assertThat(FxTest.onFx(() -> pages.get(id).requests)).isEqualTo(1);
    assertThat(FxTest.onFx(() -> stage.getScene().getFocusOwner()))
        .isNotInstanceOf(ToggleButton.class);
  }

  private static final class TrackedFocus implements InitialFocus {
    private final InitialFocus delegate;
    private int requests;

    private TrackedFocus(InitialFocus delegate) {
      this.delegate = delegate;
    }

    @Override
    public Node initialFocusTarget() {
      return delegate.initialFocusTarget();
    }

    @Override
    public void requestInitialFocus() {
      requests++;
      delegate.requestInitialFocus();
    }
  }
}
