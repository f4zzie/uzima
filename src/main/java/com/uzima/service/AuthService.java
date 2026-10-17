package com.uzima.service;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.common.Validator;
import com.uzima.domain.user.PasswordPolicy;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

/**
 * First-run setup, sign-in with lockout, password changes and the idle lock screen.
 *
 * <p>Failed sign-ins all return {@link LoginResult#FAILURE_MESSAGE}; the audit log records the real
 * reason. A BCrypt check runs even for unknown usernames so response time doesn't reveal which
 * usernames exist.
 */
public final class AuthService {

  private final UserRepository users;
  private final PasswordHasher hasher;
  private final Session session;
  private final AuditLog audit;
  private final Transactions tx;
  private final Clock clock;
  private volatile String dummyHash;

  public AuthService(
      UserRepository users,
      PasswordHasher hasher,
      Session session,
      AuditLog audit,
      Transactions tx,
      Clock clock) {
    this.users = users;
    this.hasher = hasher;
    this.session = session;
    this.audit = audit;
    this.tx = tx;
    this.clock = clock;
  }

  /** True on first run, when there are no accounts at all. */
  public boolean needsAdministrator() {
    return users.count() == 0;
  }

  /** Creates the first administrator and signs them in. Only allowed while there are no users. */
  public User createAdministrator(String username, String displayName, char[] password) {
    User created =
        tx.inTransaction(
            () -> {
              if (users.count() > 0) {
                throw new DomainException("An administrator already exists. Please sign in.");
              }
              User user = newUser(username, displayName, Role.ADMIN, password, false);
              User saved = users.insert(user, clock.instant());
              audit.recordFor(
                  saved, AuditAction.ADMINISTRATOR_CREATED, "user", saved.id(), "role: Admin");
              return saved;
            });
    session.signIn(created);
    return created;
  }

  public LoginResult login(String username, char[] password) {
    Optional<User> found = findQuietly(username);
    if (found.isEmpty()) {
      hasher.matches(password, dummyHash());
      tx.run(
          () ->
              audit.recordAnonymous(
                  username == null ? "" : username, AuditAction.LOGIN_FAILED, "Unknown username"));
      return new LoginResult.Failure(LoginResult.FAILURE_MESSAGE);
    }

    User user = found.get();
    Instant now = clock.instant();
    boolean passwordMatches = hasher.matches(password, user.passwordHash());
    if (!user.active()) {
      tx.run(() -> audit.recordFor(user, AuditAction.LOGIN_FAILED, "user", user.id(), "Inactive"));
      return new LoginResult.Failure(LoginResult.FAILURE_MESSAGE);
    }
    if (user.isLockedAt(now)) {
      tx.run(
          () ->
              audit.recordFor(
                  user,
                  AuditAction.LOGIN_FAILED,
                  "user",
                  user.id(),
                  "Account locked until " + user.lockedUntil().orElseThrow()));
      return new LoginResult.Failure(LoginResult.FAILURE_MESSAGE);
    }
    if (!passwordMatches) {
      recordFailure(user, now, "Wrong password");
      return new LoginResult.Failure(LoginResult.FAILURE_MESSAGE);
    }

    User signedIn = user.recordSuccessfulLogin();
    tx.run(
        () -> {
          users.update(signedIn);
          audit.recordFor(signedIn, AuditAction.LOGIN_SUCCEEDED, "user", signedIn.id(), "");
        });
    session.signIn(signedIn);
    return new LoginResult.Success(signedIn, signedIn.mustChangePassword());
  }

  /** Changes the signed-in user's own password and clears any "must change" flag. */
  public void changePassword(char[] currentPassword, char[] newPassword) {
    User current = reload(session.requireUser());
    if (!hasher.matches(currentPassword, current.passwordHash())) {
      throw new ValidationException("currentPassword", "Current password is incorrect");
    }
    String candidate = new String(newPassword);
    PasswordPolicy.validate(candidate, current.username());
    if (Arrays.equals(currentPassword, newPassword)) {
      throw new ValidationException("password", "Choose a password different from the current one");
    }
    User updated = current.withPasswordHash(hasher.hash(newPassword), false);
    tx.run(
        () -> {
          users.update(updated);
          audit.record(AuditAction.PASSWORD_CHANGED, "user", updated.id(), "");
        });
    session.refresh(updated);
  }

  /**
   * Checks the signed-in user's password on the idle lock screen. Wrong attempts count toward the
   * same lockout as sign-in, so an unattended machine can't be brute-forced from the lock screen.
   * Returns false once the account is locked or deactivated; the UI then signs the user out.
   */
  public boolean verifyPassword(char[] password) {
    User current = reload(session.requireUser());
    Instant now = clock.instant();
    boolean matches = hasher.matches(password, current.passwordHash());
    if (!current.active() || current.isLockedAt(now)) {
      return false;
    }
    if (!matches) {
      recordFailure(current, now, "Wrong password on lock screen");
      return false;
    }
    if (current.failedLoginAttempts() > 0) {
      User reset = current.recordSuccessfulLogin();
      tx.run(() -> users.update(reset));
      session.refresh(reset);
    }
    return true;
  }

  public void signOut() {
    session
        .currentUser()
        .ifPresent(
            u -> tx.run(() -> audit.recordFor(u, AuditAction.SIGNED_OUT, "user", u.id(), "")));
    session.signOut();
  }

  /** Builds a validated, hashed, unsaved user; reports every invalid field at once. */
  private User newUser(
      String username, String displayName, Role role, char[] password, boolean mustChange) {
    Validator v = new Validator();
    Username[] name = new Username[1];
    v.capture(() -> name[0] = new Username(username));
    if (password == null) {
      v.add("password", "Password is required");
    } else {
      v.capture(() -> PasswordPolicy.validate(new String(password), name[0]));
    }
    v.check(
        displayName != null && !displayName.isBlank(), "displayName", "Display name is required");
    v.throwIfInvalid();
    return User.create(name[0], displayName, role, hasher.hash(password), mustChange);
  }

  private void recordFailure(User user, Instant now, String reason) {
    User failed = user.recordFailedLogin(now);
    tx.run(
        () -> {
          users.update(failed);
          audit.recordFor(failed, AuditAction.LOGIN_FAILED, "user", failed.id(), reason);
          if (failed.isLockedAt(now)) {
            audit.recordFor(
                failed,
                AuditAction.ACCOUNT_LOCKED,
                "user",
                failed.id(),
                "Locked until " + failed.lockedUntil().orElseThrow());
          }
        });
  }

  private Optional<User> findQuietly(String username) {
    try {
      return users.findByUsername(new Username(username));
    } catch (ValidationException notAUsername) {
      return Optional.empty();
    }
  }

  private User reload(User user) {
    return users
        .findById(user.id())
        .orElseThrow(() -> new IllegalStateException("Signed-in user no longer exists"));
  }

  private String dummyHash() {
    String h = dummyHash;
    if (h == null) {
      h = hasher.hash("timing-equaliser-not-a-password".toCharArray());
      dummyHash = h;
    }
    return h;
  }
}
