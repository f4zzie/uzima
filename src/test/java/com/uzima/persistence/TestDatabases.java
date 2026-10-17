package com.uzima.persistence;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Fresh, migrated databases for integration tests. H2 runs in memory; MySQL runs in one shared
 * Testcontainers container (one new schema per call) and is skipped when Docker is unavailable.
 */
public final class TestDatabases {

  public enum Kind {
    H2,
    MYSQL
  }

  private static final AtomicInteger MYSQL_SCHEMAS = new AtomicInteger();
  private static MySQLContainer mysql;

  private TestDatabases() {}

  public static Database open(Kind kind) {
    return switch (kind) {
      case H2 -> h2();
      case MYSQL -> mysql();
    };
  }

  public static Database h2() {
    Database db =
        Database.open(
            new DatabaseConfig(
                "jdbc:h2:mem:"
                    + UUID.randomUUID()
                    + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
                    + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
                "sa",
                "",
                10));
    db.migrate();
    return db;
  }

  public static Database mysql() {
    assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker is not available; skipping MySQL tests");
    MySQLContainer container = container();
    String schema = "uzima_test_" + MYSQL_SCHEMAS.incrementAndGet();
    try (Connection c =
            DriverManager.getConnection(container.getJdbcUrl(), "root", container.getPassword());
        Statement s = c.createStatement()) {
      s.execute("CREATE DATABASE " + schema);
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot create test schema", e);
    }
    String url =
        container.getJdbcUrl().replaceFirst("/[^/?]+(\\?|$)", "/" + schema + "$1")
            + (container.getJdbcUrl().contains("?") ? "&" : "?")
            + "connectionTimeZone=UTC";
    Database db = Database.open(new DatabaseConfig(url, "root", container.getPassword(), 10));
    db.migrate();
    return db;
  }

  private static synchronized MySQLContainer container() {
    if (mysql == null) {
      mysql = new MySQLContainer("mysql:8.4").withUsername("root").withPassword("test");
      mysql.start();
    }
    return mysql;
  }

  /** Inserts a user row directly so tests can satisfy created_by / received_by foreign keys. */
  public static long insertUser(Database db, String username, String role) {
    Jdbc jdbc = new Jdbc(new JdbcTransactions(db.dataSource()));
    return jdbc.insert(
        "INSERT INTO users (username, display_name, role, password_hash, created_at)"
            + " VALUES (?, ?, ?, ?, ?)",
        username,
        username,
        role,
        "not-a-real-hash",
        Instant.parse("2026-01-01T00:00:00Z"));
  }
}
