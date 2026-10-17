package com.uzima.app;

import com.uzima.ui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JavaFX application lifecycle. Wiring happens in {@link CompositionRoot}. */
public final class UzimaApp extends Application {

  private static final Logger log = LoggerFactory.getLogger(UzimaApp.class);

  private CompositionRoot root;
  private Exception startupFailure;

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
    new MainWindow(root.databaseDescription()).show(stage);
    log.info("Main window shown");
  }

  @Override
  public void stop() {
    if (root != null) {
      root.close();
    }
  }
}
