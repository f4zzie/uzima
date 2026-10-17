package com.uzima.app;

import com.uzima.persistence.Database;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 * Builds the object graph by hand. Every long-lived object is created here and passed down through
 * constructors; nothing else in the app uses static singletons.
 */
public final class CompositionRoot implements AutoCloseable {

  private final AppConfig config;
  private final Database database;
  private final String databaseDescription;

  private CompositionRoot(AppConfig config, Database database, String databaseDescription) {
    this.config = config;
    this.database = database;
    this.databaseDescription = databaseDescription;
  }

  /** Opens and migrates the database. Runs off the JavaFX thread. */
  public static CompositionRoot start(AppConfig config) {
    try {
      Files.createDirectories(config.dataDir());
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot create app data directory " + config.dataDir(), e);
    }
    Database database = Database.open(config.database());
    try {
      database.migrate();
      return new CompositionRoot(config, database, database.describe());
    } catch (RuntimeException e) {
      database.close();
      throw e;
    }
  }

  public AppConfig config() {
    return config;
  }

  public String databaseDescription() {
    return databaseDescription;
  }

  @Override
  public void close() {
    database.close();
  }
}
