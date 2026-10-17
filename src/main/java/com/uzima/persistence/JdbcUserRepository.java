package com.uzima.persistence;

import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import com.uzima.service.Theme;
import com.uzima.service.UserRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class JdbcUserRepository implements UserRepository {

  private static final String COLUMNS =
      "id, username, display_name, role, password_hash, active, must_change_password,"
          + " failed_login_attempts, locked_until";

  private final Jdbc jdbc;

  public JdbcUserRepository(Jdbc jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public User insert(User u, Instant createdAt) {
    long id =
        jdbc.insert(
            "INSERT INTO users (username, display_name, role, password_hash, active,"
                + " must_change_password, failed_login_attempts, locked_until, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            u.username().value(),
            u.displayName(),
            u.role(),
            u.passwordHash(),
            u.active(),
            u.mustChangePassword(),
            u.failedLoginAttempts(),
            u.lockedUntil().orElse(null),
            createdAt);
    return new User(
        id,
        u.username(),
        u.displayName(),
        u.role(),
        u.passwordHash(),
        u.active(),
        u.mustChangePassword(),
        u.failedLoginAttempts(),
        u.lockedUntil());
  }

  @Override
  public void update(User u) {
    int rows =
        jdbc.update(
            "UPDATE users SET display_name = ?, role = ?, password_hash = ?, active = ?,"
                + " must_change_password = ?, failed_login_attempts = ?, locked_until = ?"
                + " WHERE id = ?",
            u.displayName(),
            u.role(),
            u.passwordHash(),
            u.active(),
            u.mustChangePassword(),
            u.failedLoginAttempts(),
            u.lockedUntil().orElse(null),
            u.id());
    if (rows != 1) {
      throw new IllegalStateException("No user with id " + u.id());
    }
  }

  @Override
  public Optional<User> findById(long id) {
    return jdbc.queryOne(
        "SELECT " + COLUMNS + " FROM users WHERE id = ?", JdbcUserRepository::map, id);
  }

  @Override
  public Optional<User> findByUsername(Username username) {
    return jdbc.queryOne(
        "SELECT " + COLUMNS + " FROM users WHERE username = ?",
        JdbcUserRepository::map,
        username.value());
  }

  @Override
  public List<User> findAll() {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM users ORDER BY active DESC, username",
        JdbcUserRepository::map);
  }

  @Override
  public long count() {
    return jdbc.queryOne("SELECT COUNT(*) FROM users", rs -> rs.getLong(1)).orElseThrow();
  }

  @Override
  public long countActiveAdmins() {
    return jdbc.queryOne(
            "SELECT COUNT(*) FROM users WHERE role = ? AND active = TRUE",
            rs -> rs.getLong(1),
            Role.ADMIN)
        .orElseThrow();
  }

  @Override
  public Theme theme(long userId) {
    return jdbc.queryOne(
            "SELECT theme FROM users WHERE id = ?",
            rs -> Jdbc.enumValue(rs, "theme", Theme.class),
            userId)
        .orElseThrow(() -> new IllegalStateException("No user with id " + userId));
  }

  @Override
  public void setTheme(long userId, Theme theme) {
    jdbc.update("UPDATE users SET theme = ? WHERE id = ?", theme, userId);
  }

  private static User map(ResultSet rs) throws SQLException {
    return new User(
        rs.getLong("id"),
        new Username(rs.getString("username")),
        rs.getString("display_name"),
        Jdbc.enumValue(rs, "role", Role.class),
        rs.getString("password_hash"),
        rs.getBoolean("active"),
        rs.getBoolean("must_change_password"),
        rs.getInt("failed_login_attempts"),
        Optional.ofNullable(Jdbc.instant(rs, "locked_until")));
  }
}
