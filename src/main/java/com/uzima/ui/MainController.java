package com.uzima.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Controller for the application shell. */
public final class MainController {

  private final String databaseDescription;

  @FXML private Label statusLabel;

  public MainController(String databaseDescription) {
    this.databaseDescription = databaseDescription;
  }

  @FXML
  private void initialize() {
    statusLabel.setText("Connected to " + databaseDescription);
  }
}
