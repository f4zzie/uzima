package com.uzima.persistence;

/**
 * Unchecked wrapper for {@link java.sql.SQLException} so callers above the persistence layer never
 * see JDBC types.
 */
public class PersistenceException extends RuntimeException {

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
