package com.uzima.app;

import com.uzima.ui.AppFlow;
import com.uzima.ui.Backend;
import com.uzima.ui.UiTheme;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JavaFX application lifecycle. Wiring happens in {@link CompositionRoot}. */
public final class UzimaApp extends Application {

  private static final Logger log = LoggerFactory.getLogger(UzimaApp.class);

  private CompositionRoot root;
  private Exception startupFailure;
  private Backend backend;

  @Override
  public void init() {
    // init() runs on the launcher thread, so DB startup never blocks the FX thread.
    try {
      root = CompositionRoot.start(AppConfig.load(AppDirectories.resolve()));
    } catch (Exception e) {
      log.error("Startup failed", e);
      startupFailure = e;
    }
  }

  @Override
  public void start(Stage stage) {
    if (startupFailure != null) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Uzima");
      alert.setHeaderText("Uzima could not start");
      alert.setContentText(
          startupFailure.getMessage()
              + "\n\nDetails are in the log folder: "
              + AppDirectories.resolve().resolve("logs"));
      alert.showAndWait();
      Platform.exit();
      return;
    }
    backend = new Backend(root.services());
    // Pages are registered here as they are built; the shell itself knows none of them.
    AppFlow flow = new AppFlow(backend, List.of());
    stage.setTitle("Uzima");
    stage.setMinWidth(1024);
    stage.setMinHeight(640);
    stage.setScene(UiTheme.apply(new Scene(flow.root(), 1280, 800)));
    stage.show();
    flow.start();
    log.info("Main window shown ({})", root.databaseDescription());
  }

  @Override
  public void stop() {
    if (backend != null) {
      backend.close();
    }
    if (root != null) {
      root.close();
    }
  }
}
