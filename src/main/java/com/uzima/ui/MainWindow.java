package com.uzima.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Loads the application shell and shows it on the primary stage. */
public final class MainWindow {

  private static final String VIEW = "view/main.fxml";

  private final String databaseDescription;

  public MainWindow(String databaseDescription) {
    this.databaseDescription = databaseDescription;
  }

  public void show(Stage stage) {
    FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource(VIEW));
    loader.setControllerFactory(type -> new MainController(databaseDescription));
    Parent root;
    try {
      root = loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot load " + VIEW, e);
    }
    Scene scene = UiTheme.apply(new Scene(root, 1200, 760));
    stage.setTitle("Uzima");
    stage.setMinWidth(960);
    stage.setMinHeight(600);
    stage.setScene(scene);
    stage.show();
  }
}
