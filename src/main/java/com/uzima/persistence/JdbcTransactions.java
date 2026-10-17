package com.uzima.persistence;

import com.uzima.service.Transactions;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

/**
 * Binds one JDBC connection to the current thread for the duration of a transaction. Repositories
 * get their connection through {@link #connection(ConnectionUser)}, so they join the transaction
 * when there is one and use a short auto-commit connection when there isn't.
 */
public final class JdbcTransactions implements Transactions {

  /** Work that needs a connection. */
  @FunctionalInterface
  public interface ConnectionUser<T> {
    T apply(Connection connection) throws SQLException;
  }

  private final DataSource dataSource;
  private final ThreadLocal<Connection> current = new ThreadLocal<>();

  public JdbcTransactions(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public <T> T inTransaction(Supplier<T> work) {
    if (current.get() != null) {
      return work.get();
    }
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      current.set(c);
      try {
        T result = work.get();
        c.commit();
        return result;
      } catch (RuntimeException | Error e) {
        rollbackQuietly(c, e);
        throw e;
      } finally {
        current.remove();
        c.setAutoCommit(true);
      }
    } catch (SQLException e) {
      throw new PersistenceException("Transaction failed", e);
    }
  }

  /** Runs {@code user} on the transaction's connection, or on a fresh auto-commit one. */
  public <T> T connection(ConnectionUser<T> user) {
    Connection bound = current.get();
    try {
      if (bound != null) {
        return user.apply(bound);
      }
      try (Connection c = dataSource.getConnection()) {
        return user.apply(c);
      }
    } catch (SQLException e) {
      throw PersistenceException.from(e);
    }
  }

  /**
   * Runs {@code user} on a separate auto-commit connection, independent of any transaction bound to
   * this thread. Its work is committed immediately and holds no locks afterwards. Use sparingly:
   * the calling thread briefly holds two pooled connections.
   */
  public <T> T withNewConnection(ConnectionUser<T> user) {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(true);
      return user.apply(c);
    } catch (SQLException e) {
      throw PersistenceException.from(e);
    }
  }

  /** True when called inside {@link #inTransaction}. */
  public boolean inTransaction() {
    return current.get() != null;
  }

  private static void rollbackQuietly(Connection c, Throwable cause) {
    try {
      c.rollback();
    } catch (SQLException rollbackFailure) {
      cause.addSuppressed(rollbackFailure);
    }
  }
}
