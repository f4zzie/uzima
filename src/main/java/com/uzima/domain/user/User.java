package com.uzima.domain.user;

import com.uzima.domain.common.Validator;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A staff account. {@code passwordHash} is an opaque BCrypt string produced by the service layer;
 * it never appears in {@link #toString()}.
 *
 * <p>Lockout: {@value #MAX_FAILED_ATTEMPTS} consecutive failed logins lock the account for {@link
 * #LOCK_DURATION}. Failures while locked are ignored (they neither count nor extend the lock), and
 * the counter starts fresh once the lock expires.
 */
public record User(
    long id,
    Username username,
    String displayName,
    Role role,
    String passwordHash,
    boolean active,
    boolean mustChangePassword,
    int failedLoginAttempts,
    Optional<Instant> lockedUntil) {

  public static final int MAX_FAILED_ATTEMPTS = 5;
  public static final Duration LOCK_DURATION = Duration.ofMinutes(15);
  public static final int MAX_DISPLAY_NAME = 80;

  public User {
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(role, "role");
    Objects.requireNonNull(passwordHash, "passwordHash");
    Objects.requireNonNull(lockedUntil, "lockedUntil");
    String name = displayName == null ? "" : displayName.trim();
    new Validator()
        .require(name, "displayName", "Display name is required")
        .check(name.length() <= MAX_DISPLAY_NAME, "displayName", "Display name is too long")
        .throwIfInvalid();
    displayName = name;
    if (failedLoginAttempts < 0) {
      throw new IllegalArgumentException("failedLoginAttempts cannot be negative");
    }
  }

  /** A new, active, unsaved account (id 0). */
  public static User create(
      Username username,
      String displayName,
      Role role,
      String passwordHash,
      boolean mustChangePassword) {
    return new User(
        0,
        username,
        displayName,
        role,
        passwordHash,
        true,
        mustChangePassword,
        0,
        Optional.empty());
  }

  public boolean isLockedAt(Instant now) {
    return lockedUntil.map(now::isBefore).orElse(false);
  }

  public User recordFailedLogin(Instant now) {
    if (isLockedAt(now)) {
      return this;
    }
    // An expired lock means the counter starts fresh.
    int attempts = (lockedUntil.isPresent() ? 0 : failedLoginAttempts) + 1;
    if (attempts >= MAX_FAILED_ATTEMPTS) {
      return withLoginState(0, Optional.of(now.plus(LOCK_DURATION)));
    }
    return withLoginState(attempts, Optional.empty());
  }

  public User recordSuccessfulLogin() {
    return withLoginState(0, Optional.empty());
  }

  /** Sets a new password hash. Also clears any lockout, e.g. after an admin reset. */
  public User withPasswordHash(String hash, boolean mustChange) {
    return new User(id, username, displayName, role, hash, active, mustChange, 0, Optional.empty());
  }

  public User withRole(Role newRole) {
    return new User(
        id,
        username,
        displayName,
        newRole,
        passwordHash,
        active,
        mustChangePassword,
        failedLoginAttempts,
        lockedUntil);
  }

  public User deactivated() {
    return withActive(false);
  }

  public User reactivated() {
    return withActive(true);
  }

  private User withActive(boolean value) {
    return new User(
        id,
        username,
        displayName,
        role,
        passwordHash,
        value,
        mustChangePassword,
        failedLoginAttempts,
        lockedUntil);
  }

  private User withLoginState(int attempts, Optional<Instant> lock) {
    return new User(
        id, username, displayName, role, passwordHash, active, mustChangePassword, attempts, lock);
  }

  @Override
  public String toString() {
    return "User[id="
        + id
        + ", username="
        + username
        + ", displayName="
        + displayName
        + ", role="
        + role
        + ", active="
        + active
        + ", mustChangePassword="
        + mustChangePassword
        + ", failedLoginAttempts="
        + failedLoginAttempts
        + ", lockedUntil="
        + lockedUntil
        + "]";
  }
}
