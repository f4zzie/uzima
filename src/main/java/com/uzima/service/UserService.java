package com.uzima.service;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.common.Validator;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import java.time.Clock;
import java.util.List;

/**
 * Account administration (ADMIN only) and per-user preferences. There is always at least one active
 * administrator: the last one cannot be demoted or deactivated.
 */
public final class UserService {

  private final UserRepository users;
  private final PasswordHasher hasher;
  private final TemporaryPasswords temporaryPasswords;
  private final Session session;
  private final AuditLog audit;
  private final Transactions tx;
  private final Clock clock;

  public UserService(
      UserRepository users,
      PasswordHasher hasher,
      TemporaryPasswords temporaryPasswords,
      Session session,
      AuditLog audit,
      Transactions tx,
      Clock clock) {
    this.users = users;
    this.hasher = hasher;
    this.temporaryPasswords = temporaryPasswords;
    this.session = session;
    this.audit = audit;
    this.tx = tx;
    this.clock = clock;
  }

  public List<User> list() {
    session.require(Permission.MANAGE_USERS);
    return users.findAll();
  }

  /**
   * Creates an account with a temporary password the user must change at first sign-in. The
   * returned password is shown once to the admin.
   */
  public CreatedUser create(String username, String displayName, Role role) {
    session.require(Permission.MANAGE_USERS);
    Validator v = new Validator();
    Username[] name = new Username[1];
    v.capture(() -> name[0] = new Username(username));
    v.check(
            displayName != null && !displayName.isBlank(),
            "displayName",
            "Display name is required")
        .check(role != null, "role", "Choose a role");
    v.throwIfInvalid();

    String temporary = temporaryPasswords.generate();
    return tx.inTransaction(
        () -> {
          if (users.findByUsername(name[0]).isPresent()) {
            throw new ValidationException("username", "That username is already taken");
          }
          User saved =
              users.insert(
                  User.create(
                      name[0], displayName, role, hasher.hash(temporary.toCharArray()), true),
                  clock.instant());
          audit.record(
              AuditAction.USER_CREATED,
              "user",
              saved.id(),
              "username: " + saved.username() + "; role: " + role.label());
          return new CreatedUser(saved, temporary);
        });
  }

  public User changeRole(long userId, Role newRole) {
    session.require(Permission.MANAGE_USERS);
    return tx.inTransaction(
        () -> {
          User target = load(userId);
          if (target.role() == newRole) {
            return target;
          }
          if (isLastActiveAdmin(target)) {
            throw new DomainException(
                "There must be at least one active administrator. Make someone else an"
                    + " administrator first.");
          }
          User updated = target.withRole(newRole);
          users.update(updated);
          audit.record(
              AuditAction.USER_ROLE_CHANGED,
              "user",
              userId,
              new Changes().field("role", target.role().label(), newRole.label()).toString());
          session.refresh(updated);
          return updated;
        });
  }

  public User deactivate(long userId) {
    User actor = session.require(Permission.MANAGE_USERS);
    if (actor.id() == userId) {
      throw new DomainException("You cannot deactivate your own account.");
    }
    return tx.inTransaction(
        () -> {
          User target = load(userId);
          if (!target.active()) {
            return target;
          }
          if (isLastActiveAdmin(target)) {
            throw new DomainException("The last active administrator cannot be deactivated.");
          }
          User updated = target.deactivated();
          users.update(updated);
          audit.record(AuditAction.USER_DEACTIVATED, "user", userId, "");
          return updated;
        });
  }

  public User reactivate(long userId) {
    session.require(Permission.MANAGE_USERS);
    return tx.inTransaction(
        () -> {
          User target = load(userId);
          if (target.active()) {
            return target;
          }
          User updated = target.reactivated();
          users.update(updated);
          audit.record(AuditAction.USER_REACTIVATED, "user", userId, "");
          return updated;
        });
  }

  /**
   * Gives the user a new temporary password, clears any lockout and makes them change it at next
   * sign-in. Returns the temporary password to show to the admin once.
   */
  public String resetPassword(long userId) {
    session.require(Permission.MANAGE_USERS);
    String temporary = temporaryPasswords.generate();
    tx.run(
        () -> {
          User target = load(userId);
          User updated = target.withPasswordHash(hasher.hash(temporary.toCharArray()), true);
          users.update(updated);
          audit.record(AuditAction.PASSWORD_RESET, "user", userId, "");
          session.refresh(updated);
        });
    return temporary;
  }

  /** The signed-in user's colour scheme. */
  public Theme theme() {
    return users.theme(session.requireUser().id());
  }

  /** A personal display preference, so it needs no permission and isn't audited. */
  public void setTheme(Theme theme) {
    users.setTheme(session.requireUser().id(), theme);
  }

  private User load(long userId) {
    return users
        .findById(userId)
        .orElseThrow(() -> new DomainException("That user no longer exists."));
  }

  private boolean isLastActiveAdmin(User target) {
    return target.active() && target.role() == Role.ADMIN && users.countActiveAdmins() <= 1;
  }
}
