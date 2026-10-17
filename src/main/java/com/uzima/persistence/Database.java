package com.uzima.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns the connection pool and applies Flyway migrations. One instance per running app. */
public final class Database implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(Database.class);
  static final String MIGRATIONS = "classpath:db/migration";

  private final HikariDataSource dataSource;

  private Database(HikariDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static Database open(DatabaseConfig config) {
    log.info("Opening database {}", JdbcUrls.redact(config.url()));
    HikariConfig hikari = new HikariConfig();
    hikari.setPoolName("uzima");
    hikari.setJdbcUrl(config.url());
    hikari.setUsername(config.user());
    hikari.setPassword(config.password());
    hikari.setMaximumPoolSize(config.maxPoolSize());
    hikari.setAutoCommit(true);
    return new Database(new HikariDataSource(hikari));
  }

  /** Applies pending migrations and returns the current schema version, or "none". */
  public String migrate() {
    Flyway flyway =
        Flyway.configure().dataSource(dataSource).locations(MIGRATIONS).cleanDisabled(true).load();
    MigrateResult result = flyway.migrate();
    MigrationInfo current = flyway.info().current();
    String version = current == null ? "none" : current.getVersion().getVersion();
    log.info("Migrations applied: {}, schema version: {}", result.migrationsExecuted, version);
    return version;
  }

  /** Product name and version of the connected database, e.g. "H2 2.4.240 (2025-09-22)". */
  public String describe() {
    try (Connection c = dataSource.getConnection()) {
      DatabaseMetaData meta = c.getMetaData();
      return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
    } catch (SQLException e) {
      throw new PersistenceException("Cannot read database metadata", e);
    }
  }

  public DataSource dataSource() {
    return dataSource;
  }

  @Override
  public void close() {
    dataSource.close();
  }
}
