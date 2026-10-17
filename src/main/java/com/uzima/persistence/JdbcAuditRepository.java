package com.uzima.persistence;

import com.uzima.service.AuditAction;
import com.uzima.service.AuditEntry;
import com.uzima.service.AuditQuery;
import com.uzima.service.AuditRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Audit log rows. Insert and select only: there is no update or delete. */
public final class JdbcAuditRepository implements AuditRepository {

  private final Jdbc jdbc;

  public JdbcAuditRepository(Jdbc jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public long append(AuditEntry e) {
    return jdbc.insert(
        "INSERT INTO audit_log (occurred_at, user_id, username, action, entity_type, entity_id,"
            + " details) VALUES (?, ?, ?, ?, ?, ?, ?)",
        e.occurredAt(),
        e.userId(),
        e.username(),
        e.action(),
        e.entityType(),
        e.entityId(),
        e.details());
  }

  @Override
  public List<AuditEntry> search(AuditQuery q) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT id, occurred_at, user_id, username, action, entity_type, entity_id, details"
                + " FROM audit_log WHERE 1 = 1");
    List<Object> params = new ArrayList<>();
    q.from().ifPresent(v -> add(sql, params, " AND occurred_at >= ?", v));
    q.to().ifPresent(v -> add(sql, params, " AND occurred_at < ?", v));
    q.action().ifPresent(v -> add(sql, params, " AND action = ?", v));
    q.userId().ifPresent(v -> add(sql, params, " AND user_id = ?", v));
    q.text()
        .filter(t -> !t.isBlank())
        .ifPresent(
            t -> {
              String like = "%" + escapeLike(t.strip().toLowerCase()) + "%";
              sql.append(
                  " AND (LOWER(details) LIKE ? ESCAPE '!' OR LOWER(username) LIKE ? ESCAPE '!'"
                      + " OR LOWER(entity_id) LIKE ? ESCAPE '!')");
              params.add(like);
              params.add(like);
              params.add(like);
            });
    sql.append(" ORDER BY occurred_at DESC, id DESC LIMIT ?");
    params.add(Math.max(1, q.limit()));
    return jdbc.query(sql.toString(), JdbcAuditRepository::map, params.toArray());
  }

  private static void add(StringBuilder sql, List<Object> params, String clause, Object value) {
    sql.append(clause);
    params.add(value);
  }

  static String escapeLike(String text) {
    return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
  }

  private static AuditEntry map(ResultSet rs) throws SQLException {
    return new AuditEntry(
        rs.getLong("id"),
        Jdbc.instant(rs, "occurred_at"),
        Jdbc.nullableLong(rs, "user_id"),
        rs.getString("username"),
        Jdbc.enumValue(rs, "action", AuditAction.class),
        rs.getString("entity_type"),
        rs.getString("entity_id"),
        rs.getString("details"));
  }
}
