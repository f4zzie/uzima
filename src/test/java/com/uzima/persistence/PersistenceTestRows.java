package com.uzima.persistence;

import java.time.Instant;

/** Minimal raw rows for tests that only need a foreign key to exist. */
final class PersistenceTestRows {

  private PersistenceTestRows() {}

  /** Inserts a bare pending job for {@code clientId}, optionally using a drilling type. */
  static long insertJob(Database db, long clientId, long userId, Long drillingTypeId) {
    Jdbc jdbc = new Jdbc(new JdbcTransactions(db.dataSource()));
    Instant now = Instant.parse("2026-10-01T07:00:00Z");
    return jdbc.insert(
        "INSERT INTO jobs (job_number, client_id, status, drilling_type_id, priced_category,"
            + " survey_fee, authority_fee, tax_rate, subtotal, tax_amount, grand_total,"
            + " created_at, created_by, updated_at)"
            + " VALUES (?, ?, 'PENDING_SURVEY', ?, 'DOMESTIC', 7000, 10000, 0.16, 17000, 2720,"
            + " 19720, ?, ?, ?)",
        "JOB-2026-" + System.nanoTime() % 100_000,
        clientId,
        drillingTypeId,
        now,
        userId,
        now);
  }
}
