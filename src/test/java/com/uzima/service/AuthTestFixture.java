package com.uzima.service;

import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.persistence.JdbcSettingsRepository;
import com.uzima.persistence.JdbcUserRepository;

/** Auth, user and settings services wired on a {@link ServiceTestContext} with fast hashing. */
final class AuthTestFixture implements AutoCloseable {

  static final String ADMIN_PASSWORD = "admin-password-1";

  final ServiceTestContext ctx = ServiceTestContext.h2();
  final JdbcUserRepository users = new JdbcUserRepository(ctx.jdbc);
  final BcryptPasswordHasher hasher = new BcryptPasswordHasher(4);
  final AuthService auth =
      new AuthService(users, hasher, ctx.session, ctx.audit, ctx.tx, ctx.clock);
  final UserService userService =
      new UserService(
          users, hasher, new TemporaryPasswords(), ctx.session, ctx.audit, ctx.tx, ctx.clock);
  final SettingsService settings =
      new SettingsService(new JdbcSettingsRepository(ctx.jdbc), ctx.session, ctx.audit, ctx.tx);

  /** Creates the first administrator (signed in afterwards). */
  User admin() {
    return auth.createAdministrator("admin", "Administrator", ADMIN_PASSWORD.toCharArray());
  }

  /**
   * Creates a user as the signed-in admin and sets a known password, so tests can sign in as them.
   * Leaves the admin signed in.
   */
  User user(String username, Role role, String password) {
    CreatedUser created = userService.create(username, username, role);
    User withPassword = created.user().withPasswordHash(hasher.hash(password.toCharArray()), false);
    users.update(withPassword);
    return withPassword;
  }

  long count(AuditAction action) {
    return ctx.auditEntries().stream().filter(e -> e.action() == action).count();
  }

  @Override
  public void close() {
    ctx.close();
  }
}
