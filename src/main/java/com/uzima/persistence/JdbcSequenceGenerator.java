package com.uzima.persistence;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.payment.ReceiptNumber;
import com.uzima.persistence.PersistenceException.DuplicateKeyException;
import com.uzima.service.SequenceGenerator;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Year;

/**
 * Row-locked counters in the {@code sequences} table. {@code SELECT ... FOR UPDATE} holds the row
 * until the caller's transaction ends, so concurrent callers queue instead of reading the same
 * value, and a rolled-back transaction gives its number back. Never {@code MAX(...) + 1}.
 */
public final class JdbcSequenceGenerator implements SequenceGenerator {

  private final JdbcTransactions tx;
  private final Jdbc jdbc;

  public JdbcSequenceGenerator(JdbcTransactions tx, Jdbc jdbc) {
    this.tx = tx;
    this.jdbc = jdbc;
  }

  @Override
  public ClientCode nextClientCode(ClientCategory category) {
    return new ClientCode(category, Math.toIntExact(next("CLIENT-" + category.code())));
  }

  @Override
  public JobNumber nextJobNumber(Year year) {
    return new JobNumber(year, Math.toIntExact(next("JOB-" + year)));
  }

  @Override
  public ReceiptNumber nextReceiptNumber(Year year) {
    return new ReceiptNumber(year, Math.toIntExact(next("RCP-" + year)));
  }

  /** Returns the next value of {@code name}, creating the counter at 1 on first use. */
  long next(String name) {
    if (!tx.inTransaction()) {
      throw new IllegalStateException("Sequence numbers must be taken inside a transaction");
    }
    boolean exists =
        jdbc.queryOne(
                "SELECT COUNT(*) FROM sequences WHERE name = ?", rs -> rs.getLong(1) > 0, name)
            .orElse(false);
    if (!exists) {
      createCounter(name);
    }
    long value =
        jdbc.queryOne(
                "SELECT next_value FROM sequences WHERE name = ? FOR UPDATE",
                rs -> rs.getLong(1),
                name)
            .orElseThrow(() -> new IllegalStateException("Sequence " + name + " is missing"));
    jdbc.update("UPDATE sequences SET next_value = ? WHERE name = ?", value + 1, name);
    return value;
  }

  /**
   * Creates a counter row outside the caller's transaction. Doing it inside would mean a locking
   * read of a missing row, which on MySQL takes a gap lock; two callers holding that gap lock and
   * both inserting deadlock. Creating the row on its own committed connection means the caller only
   * ever locks an existing row. Creating a counter never uses up a number, so it doesn't affect
   * gap-freedom.
   */
  private void createCounter(String name) {
    tx.withNewConnection(
        c -> {
          try (PreparedStatement ps =
              c.prepareStatement("INSERT INTO sequences (name, next_value) VALUES (?, 1)")) {
            ps.setString(1, name);
            ps.executeUpdate();
          } catch (SQLException e) {
            if (!(PersistenceException.from(e) instanceof DuplicateKeyException)) {
              throw e;
            }
            // Another caller created it first; that's fine.
          }
          return null;
        });
  }
}
