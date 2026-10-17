package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTransactionsTest {

  private Database db;
  private JdbcTransactions tx;
  private Jdbc jdbc;

  @BeforeEach
  void setUp() {
    db = TestDatabases.h2();
    tx = new JdbcTransactions(db.dataSource());
    jdbc = new Jdbc(tx);
  }

  @AfterEach
  void tearDown() {
    db.close();
  }

  private int settingsNamed(String key) {
    return jdbc.queryOne(
            "SELECT COUNT(*) FROM settings WHERE setting_key = ?", rs -> rs.getInt(1), key)
        .orElseThrow();
  }

  private void insertSetting(String key) {
    jdbc.update("INSERT INTO settings (setting_key, setting_value) VALUES (?, 'v')", key);
  }

  @Test
  void commitsAllStatementsTogether() {
    tx.run(
        () -> {
          insertSetting("a");
          insertSetting("b");
        });
    assertThat(settingsNamed("a") + settingsNamed("b")).isEqualTo(2);
  }

  @Test
  void rollsBackEverythingWhenWorkFails() {
    assertThatThrownBy(
            () ->
                tx.run(
                    () -> {
                      insertSetting("a");
                      throw new IllegalStateException("boom");
                    }))
        .hasMessage("boom");
    assertThat(settingsNamed("a")).isZero();
  }

  @Test
  void nestedCallsJoinTheOuterTransaction() {
    assertThatThrownBy(
            () ->
                tx.run(
                    () -> {
                      tx.run(() -> insertSetting("inner"));
                      throw new IllegalStateException("outer fails");
                    }))
        .isInstanceOf(IllegalStateException.class);
    assertThat(settingsNamed("inner")).isZero();
  }

  @Test
  void outsideATransactionEachStatementAutoCommits() {
    assertThat(tx.inTransaction()).isFalse();
    insertSetting("solo");
    assertThat(settingsNamed("solo")).isEqualTo(1);
  }

  @Test
  void duplicateKeysAreReportedAsDuplicateKeyException() {
    insertSetting("dup");
    assertThatThrownBy(() -> insertSetting("dup"))
        .isInstanceOf(PersistenceException.DuplicateKeyException.class);
  }
}
