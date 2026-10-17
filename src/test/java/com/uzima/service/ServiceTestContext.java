package com.uzima.service;

import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import com.uzima.persistence.Database;
import com.uzima.persistence.Jdbc;
import com.uzima.persistence.JdbcAuditRepository;
import com.uzima.persistence.JdbcSequenceGenerator;
import com.uzima.persistence.JdbcTransactions;
import com.uzima.persistence.TestDatabases;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Shared wiring for service integration tests: a fresh migrated database plus the cross-cutting
 * pieces every service needs. Tests build the services they exercise on top of it.
 */
public final class ServiceTestContext implements AutoCloseable {

  public static final Instant START = Instant.parse("2026-10-01T07:00:00Z");

  public final Database database;
  public final JdbcTransactions tx;
  public final Jdbc jdbc;
  public final MutableClock clock = new MutableClock(START, DisplayFormats.ZONE);
  public final Session session = new Session();
  public final JdbcAuditRepository auditRepository;
  public final AuditLog audit;
  public final JdbcSequenceGenerator sequences;

  private ServiceTestContext(Database database) {
    this.database = database;
    this.tx = new JdbcTransactions(database.dataSource());
    this.jdbc = new Jdbc(tx);
    this.auditRepository = new JdbcAuditRepository(jdbc);
    this.audit = new AuditLog(auditRepository, session, clock);
    this.sequences = new JdbcSequenceGenerator(tx, jdbc);
  }

  public static ServiceTestContext h2() {
    return new ServiceTestContext(TestDatabases.h2());
  }

  public static ServiceTestContext open(TestDatabases.Kind kind) {
    return new ServiceTestContext(TestDatabases.open(kind));
  }

  /**
   * Inserts a user row with the given role (no usable password) and signs it in. Returns the
   * signed-in user.
   */
  public User signInAs(Role role) {
    String name = role.name().toLowerCase() + "." + System.nanoTime() % 100_000;
    long id = TestDatabases.insertUser(database, name, role.name());
    User user =
        new User(
            id,
            new Username(name),
            name,
            role,
            "not-a-real-hash",
            true,
            false,
            0,
            Optional.empty());
    session.signIn(user);
    return user;
  }

  public void signOut() {
    session.signOut();
  }

  /** Every audit entry, newest first. */
  public List<AuditEntry> auditEntries() {
    return auditRepository.search(AuditQuery.latest(1000));
  }

  @Override
  public void close() {
    database.close();
  }
}
