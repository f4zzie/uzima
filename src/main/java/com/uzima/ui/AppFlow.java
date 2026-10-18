package com.uzima.ui;

import com.uzima.domain.user.User;
import com.uzima.service.LoginResult;
import com.uzima.ui.auth.FirstRunView;
import com.uzima.ui.auth.FirstRunViewModel;
import com.uzima.ui.auth.LoginView;
import com.uzima.ui.auth.LoginViewModel;
import com.uzima.ui.component.Banner;
import com.uzima.ui.shell.AppShell;
import com.uzima.ui.shell.Destination;
import com.uzima.ui.shell.ShellViewModel;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides which top-level screen fills the window: first-run setup, sign-in or the app shell. All
 * screens live inside one host pane, so the window (and anything layered over it) stays put.
 */
public final class AppFlow {

  private static final Logger log = LoggerFactory.getLogger(AppFlow.class);

  private final Backend backend;
  private final List<Destination> destinations;
  private final StackPane host = new StackPane();

  public AppFlow(Backend backend, List<Destination> destinations) {
    this.backend = backend;
    this.destinations = List.copyOf(destinations);
    host.getStyleClass().add("app-host");
  }

  /** The node to put in the scene. */
  public Parent root() {
    return host;
  }

  /** Checks whether this is a first run, then shows setup or sign-in. */
  public void start() {
    show(loading());
    backend.run(
        s -> s.auth().needsAdministrator(),
        needsAdmin -> {
          if (needsAdmin) {
            showFirstRun();
          } else {
            showLogin(null);
          }
        },
        error -> {
          log.error("Could not check for an administrator account", error);
          show(startupError());
        });
  }

  /** Shows sign-in, optionally with a message in its banner. */
  public void showLogin(String message) {
    LoginViewModel model = new LoginViewModel(backend, this::signedIn, message);
    show(new LoginView(model).root());
  }

  public void showFirstRun() {
    show(new FirstRunView(new FirstRunViewModel(backend, this::showShell)).root());
  }

  public void showShell(User user) {
    ShellViewModel model =
        new ShellViewModel(backend, user, destinations, null, () -> showLogin(null));
    show(new AppShell(model).root());
  }

  private void signedIn(LoginResult.Success success) {
    // Phase 3 part D routes mustChangePassword to the forced change-password screen.
    showShell(success.user());
  }

  private void show(Node screen) {
    host.getChildren().setAll(screen);
  }

  private static Node loading() {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().add("app-loading-spinner");
    Label text = new Label("Opening Uzima…");
    text.getStyleClass().add("text-muted");
    VBox box = new VBox(spinner, text);
    box.getStyleClass().addAll("app-loading", "stack-12");
    return box;
  }

  private Node startupError() {
    Banner banner = new Banner();
    banner.showError(
        "Uzima couldn't read its database. Check that it is reachable, then try again. Details"
            + " are in the log folder.");
    Button retry = new Button("Try again");
    retry.getStyleClass().add("primary");
    retry.setOnAction(e -> start());
    VBox box = new VBox(banner, retry);
    box.getStyleClass().addAll("app-loading", "stack-16");
    return box;
  }
}
