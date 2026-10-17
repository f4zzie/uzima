package com.uzima.persistence;

import java.sql.SQLException;

/**
 * Unchecked wrapper for {@link java.sql.SQLException} so callers above the persistence layer never
 * see JDBC types.
 */
public class PersistenceException extends RuntimeException {

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Wraps {@code e}, turning unique-key violations into {@link DuplicateKeyException}. */
  public static PersistenceException from(SQLException e) {
    // SQLState class 23 = integrity constraint violation; MySQL 1062 and H2 23505 = duplicate key.
    if (e.getErrorCode() == 1062 || "23505".equals(e.getSQLState())) {
      return new DuplicateKeyException(e);
    }
    return new PersistenceException("Database error (" + e.getSQLState() + ")", e);
  }

  /** A unique constraint was violated. */
  public static final class DuplicateKeyException extends PersistenceException {
    DuplicateKeyException(SQLException cause) {
      super("Duplicate key", cause);
    }
  }
}
