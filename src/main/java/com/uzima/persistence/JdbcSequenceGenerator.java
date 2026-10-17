package com.uzima.persistence;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.payment.ReceiptNumber;
import com.uzima.persistence.PersistenceException.DuplicateKeyException;
import com.uzima.service.SequenceGenerator;
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
    for (int attempt = 0; attempt < 3; attempt++) {
      var current =
          jdbc.queryOne(
              "SELECT next_value FROM sequences WHERE name = ? FOR UPDATE",
              rs -> rs.getLong(1),
              name);
      if (current.isPresent()) {
        long value = current.get();
        jdbc.update("UPDATE sequences SET next_value = ? WHERE name = ?", value + 1, name);
        return value;
      }
      try {
        jdbc.update("INSERT INTO sequences (name, next_value) VALUES (?, ?)", name, 2L);
        return 1;
      } catch (DuplicateKeyException raced) {
        // Another transaction created the counter first; loop and lock its row instead.
      }
    }
    throw new IllegalStateException("Could not allocate a number from sequence " + name);
  }
}
