package com.uzima.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/** Loads FXML views from {@code com/uzima/ui/view/} with an already-built controller. */
public final class Views {

  private Views() {}

  /** Loads {@code view/<name>.fxml} and injects {@code controller} (FXML declares none). */
  public static Parent load(String name, Object controller) {
    String resource = "view/" + name + ".fxml";
    FXMLLoader loader =
        new FXMLLoader(Objects.requireNonNull(Views.class.getResource(resource), resource));
    loader.setController(controller);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot load " + resource, e);
    }
  }
}
