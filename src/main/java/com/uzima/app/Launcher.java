package com.uzima.app;

import java.nio.file.Path;
import javafx.application.Application;

/**
 * Process entry point. Kept separate from {@link UzimaApp} so the app runs from a plain classpath
 * (fat jar, jpackage) where JavaFX refuses to start a main class that extends Application.
 */
public final class Launcher {

  private Launcher() {}

  public static void main(String[] args) {
    Path dataDir = AppDirectories.resolve();
    // Must be set before the first logger is created; logback.xml reads it.
    System.setProperty("uzima.logDir", dataDir.resolve("logs").toString());
    Application.launch(UzimaApp.class, args);
  }
}
