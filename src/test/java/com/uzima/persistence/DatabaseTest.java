package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DatabaseTest {

  private static DatabaseConfig inMemory() {
    return new DatabaseConfig(
        "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa",
        "",
        2);
  }

  @Test
  void migrateCreatesFlywayHistoryAndConnectionWorks() throws Exception {
    try (Database db = Database.open(inMemory())) {
      db.migrate();

      try (Connection c = db.dataSource().getConnection();
          ResultSet rs = c.getMetaData().getTables(null, null, "flyway_schema_history", null)) {
        assertThat(rs.next()).as("flyway_schema_history exists").isTrue();
      }
      assertThat(db.describe()).startsWith("H2 ");
    }
  }

  @Test
  void migrateIsIdempotent() {
    try (Database db = Database.open(inMemory())) {
      assertThat(db.migrate()).isEqualTo("2");
      assertThat(db.migrate()).isEqualTo("2");
    }
  }
}
