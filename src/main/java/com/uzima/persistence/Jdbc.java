package com.uzima.persistence;

import com.uzima.domain.common.Metres;
import com.uzima.domain.money.Money;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Small helper over plain JDBC: parameter binding, row mapping and the type conversions shared by
 * every repository. Instants are stored as UTC {@code DATETIME(3)}.
 */
public final class Jdbc {

  /** Maps the current row of a result set. */
  @FunctionalInterface
  public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
  }

  private final JdbcTransactions tx;

  public Jdbc(JdbcTransactions tx) {
    this.tx = tx;
  }

  public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
    return tx.connection(
        c -> {
          try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
              List<T> rows = new ArrayList<>();
              while (rs.next()) {
                rows.add(mapper.map(rs));
              }
              return rows;
            }
          }
        });
  }

  public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
    List<T> rows = query(sql, mapper, params);
    if (rows.size() > 1) {
      throw new IllegalStateException("Expected at most one row, got " + rows.size());
    }
    return rows.stream().findFirst();
  }

  /** Executes an INSERT/UPDATE/DELETE and returns the affected row count. */
  public int update(String sql, Object... params) {
    return tx.connection(
        c -> {
          try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
          }
        });
  }

  /** Executes an INSERT and returns the generated key. */
  public long insert(String sql, Object... params) {
    return tx.connection(
        c -> {
          try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
              if (!keys.next()) {
                throw new SQLException("No generated key returned");
              }
              return keys.getLong(1);
            }
          }
        });
  }

  private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
    for (int i = 0; i < params.length; i++) {
      int index = i + 1;
      switch (params[i]) {
        case null -> ps.setNull(index, Types.NULL);
        case Money m -> ps.setBigDecimal(index, m.amount());
        case Metres m -> ps.setBigDecimal(index, m.value());
        case Instant t -> ps.setObject(index, LocalDateTime.ofInstant(t, ZoneOffset.UTC));
        case LocalDate d -> ps.setObject(index, d);
        case Enum<?> e -> ps.setString(index, e.name());
        case Optional<?> o -> throw new IllegalArgumentException("Unwrap Optionals before binding");
        default -> ps.setObject(index, params[i]);
      }
    }
  }

  // --- Column readers -------------------------------------------------------------------------

  public static Money money(ResultSet rs, String column) throws SQLException {
    BigDecimal value = rs.getBigDecimal(column);
    return value == null ? null : new Money(value);
  }

  public static Metres metres(ResultSet rs, String column) throws SQLException {
    BigDecimal value = rs.getBigDecimal(column);
    return value == null ? null : new Metres(value);
  }

  public static Instant instant(ResultSet rs, String column) throws SQLException {
    // Read as LocalDateTime and interpret as UTC so the JVM time zone never shifts stored values.
    LocalDateTime value = rs.getObject(column, LocalDateTime.class);
    return value == null ? null : value.toInstant(ZoneOffset.UTC);
  }

  public static LocalDate date(ResultSet rs, String column) throws SQLException {
    return rs.getObject(column, LocalDate.class);
  }

  public static <E extends Enum<E>> E enumValue(ResultSet rs, String column, Class<E> type)
      throws SQLException {
    String value = rs.getString(column);
    return value == null ? null : Enum.valueOf(type, value);
  }

  /** Nullable BIGINT column. */
  public static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    return rs.wasNull() ? null : value;
  }
}
