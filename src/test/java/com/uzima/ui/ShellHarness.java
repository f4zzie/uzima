package com.uzima.ui;

import com.uzima.ui.shell.Destination;
import com.uzima.ui.shell.Navigator;
import com.uzima.ui.shell.SampleDestinations;
import java.util.List;
import java.util.function.Function;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Shows a real page inside the real shell for TestFX tests and snapshots: the full sidebar (the
 * other entries are the sample pages) with {@code page} registered under {@code destinationId} and
 * opened. Uses the signed-in user of the backend, normally {@link SampleData}'s administrator.
 */
public final class ShellHarness {

  private ShellHarness() {}

  /** Call on the FX thread (e.g. from a TestFX {@code @Start} method). */
  public static AppFlow show(
      Stage stage, Backend backend, String destinationId, Function<Navigator, Node> page) {
    List<Destination> destinations =
        SampleDestinations.all().stream()
            .map(
                d ->
                    d.id().equals(destinationId)
                        ? new Destination(d.id(), d.title(), d.icon(), d.permission(), page)
                        : d)
            .toList();
    AppFlow flow = new AppFlow(backend, destinations);
    // Test the app's window size, independently of the larger virtual screen used by Monocle.
    stage.setScene(UiTheme.apply(new Scene(flow.root(), 1366, 768)));
    stage.show();
    // Earlier test classes leave windows open; keyboard input and Node.isFocused() follow the
    // focused window, so make sure it is this one.
    stage.toFront();
    stage.requestFocus();
    flow.showShell(backend.session().requireUser());
    flow.navigator().orElseThrow().open(destinationId);
    return flow;
  }
}
