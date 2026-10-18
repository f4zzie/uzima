package com.uzima.service;

import static com.uzima.service.AuthTestFixture.ADMIN_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

  private final AuthTestFixture f = new AuthTestFixture();

  @AfterEach
  void close() {
    f.close();
  }

  private LoginResult login(String username, String password) {
    return f.auth.login(username, password.toCharArray());
  }

  private static String failureMessage(LoginResult result) {
    assertThat(result).isInstanceOf(LoginResult.Failure.class);
    return ((LoginResult.Failure) result).message();
  }

  @Test
  void firstRunNeedsAnAdministrator() {
    assertThat(f.auth.needsAdministrator()).isTrue();
    User admin = f.admin();
    assertThat(f.auth.needsAdministrator()).isFalse();
    assertThat(admin.role()).isEqualTo(Role.ADMIN);
    assertThat(admin.mustChangePassword()).isFalse();
    assertThat(f.ctx.session.requireUser().id()).isEqualTo(admin.id());
    assertThat(f.count(AuditAction.ADMINISTRATOR_CREATED)).isEqualTo(1);
  }

  @Test
  void storesOnlyABcryptHashOfThePassword() {
    User admin = f.admin();
    String stored = f.users.findById(admin.id()).orElseThrow().passwordHash();
    assertThat(stored)
        .startsWith("$2a$")
        .isNotEqualTo(ADMIN_PASSWORD)
        .doesNotContain(ADMIN_PASSWORD);
  }

  @Test
  void administratorCanOnlyBeCreatedOnFirstRun() {
    f.admin();
    assertThatThrownBy(
            () -> f.auth.createAdministrator("second", "Second", "another-password".toCharArray()))
        .isInstanceOf(DomainException.class);
    assertThat(f.users.count()).isEqualTo(1);
  }

  @Test
  void firstRunReportsEveryInvalidField() {
    assertThatThrownBy(() -> f.auth.createAdministrator("x", " ", "short".toCharArray()))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(err -> err.field())
                    .containsExactlyInAnyOrder("username", "password", "displayName"));
    assertThatThrownBy(() -> f.auth.createAdministrator("admin", "Admin", null))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () -> f.auth.createAdministrator("adminuser1", "A", "ADMINUSER1".toCharArray()))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("password")).isPresent());
    assertThat(f.auth.needsAdministrator()).isTrue();
  }

  @Test
  void successfulLoginSignsInAndIsAudited() {
    f.admin();
    f.auth.signOut();

    LoginResult result = login("ADMIN", ADMIN_PASSWORD);

    assertThat(result).isInstanceOf(LoginResult.Success.class);
    assertThat(((LoginResult.Success) result).mustChangePassword()).isFalse();
    assertThat(f.ctx.session.currentUser()).isPresent();
    assertThat(f.count(AuditAction.LOGIN_SUCCEEDED)).isEqualTo(1);
    assertThat(f.count(AuditAction.SIGNED_OUT)).isEqualTo(1);
  }

  @Test
  void everyFailureLooksTheSame() {
    f.admin();
    User inactive = f.user("inactive", Role.CLERK, "inactive-password");
    f.userService.deactivate(inactive.id());
    User locked = f.user("locked", Role.CLERK, "locked-password");
    f.auth.signOut();
    for (int i = 0; i < 5; i++) {
      login("locked", "wrong-password");
    }

    List<String> messages =
        List.of(
            failureMessage(login("nobody", "whatever-password")),
            failureMessage(login("admin", "wrong-password")),
            failureMessage(login("inactive", "inactive-password")),
            failureMessage(login("locked", "locked-password")),
            failureMessage(login("!!", "x")));

    assertThat(messages).containsOnly(LoginResult.FAILURE_MESSAGE);
    assertThat(LoginResult.FAILURE_MESSAGE)
        .doesNotContainIgnoringCase("exist")
        .doesNotContainIgnoringCase("not found");
    assertThat(f.ctx.session.currentUser()).isEmpty();
    assertThat(f.users.findById(locked.id()).orElseThrow().isLockedAt(f.ctx.clock.instant()))
        .isTrue();
  }

  @Test
  void unknownUsernameIsAuditedWithoutAUserId() {
    login("nobody", "whatever-password");
    AuditEntry e = f.ctx.auditEntries().getFirst();
    assertThat(e.action()).isEqualTo(AuditAction.LOGIN_FAILED);
    assertThat(e.userId()).isNull();
    assertThat(e.username()).isEqualTo("nobody");
    assertThat(e.details()).doesNotContain("whatever-password");
  }

  @Test
  void fifthConsecutiveFailureLocksForFifteenMinutes() {
    f.admin();
    f.auth.signOut();
    for (int i = 0; i < 4; i++) {
      login("admin", "wrong-password");
    }
    assertThat(f.count(AuditAction.ACCOUNT_LOCKED)).isZero();

    login("admin", "wrong-password");
    assertThat(f.count(AuditAction.ACCOUNT_LOCKED)).isEqualTo(1);
    assertThat(login("admin", ADMIN_PASSWORD)).isInstanceOf(LoginResult.Failure.class);

    f.ctx.clock.advance(Duration.ofMinutes(15).minusSeconds(1));
    assertThat(login("admin", ADMIN_PASSWORD)).isInstanceOf(LoginResult.Failure.class);

    f.ctx.clock.advance(Duration.ofSeconds(1));
    assertThat(login("admin", ADMIN_PASSWORD)).isInstanceOf(LoginResult.Success.class);
  }

  @Test
  void successResetsTheFailureCounter() {
    f.admin();
    f.auth.signOut();
    for (int i = 0; i < 4; i++) {
      login("admin", "wrong-password");
    }
    assertThat(login("admin", ADMIN_PASSWORD)).isInstanceOf(LoginResult.Success.class);
    for (int i = 0; i < 4; i++) {
      login("admin", "wrong-password");
    }
    assertThat(login("admin", ADMIN_PASSWORD)).isInstanceOf(LoginResult.Success.class);
    assertThat(f.count(AuditAction.ACCOUNT_LOCKED)).isZero();
  }

  @Test
  void changePasswordVerifiesCurrentAndAppliesPolicy() {
    f.admin();
    assertThatThrownBy(
            () ->
                f.auth.changePassword(
                    "wrong-current".toCharArray(), "brand-new-password".toCharArray()))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.messageFor("currentPassword")).isPresent());
    assertThatThrownBy(
            () -> f.auth.changePassword(ADMIN_PASSWORD.toCharArray(), "short".toCharArray()))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("password")).isPresent());
    assertThatThrownBy(
            () -> f.auth.changePassword(ADMIN_PASSWORD.toCharArray(), ADMIN_PASSWORD.toCharArray()))
        .isInstanceOf(ValidationException.class);

    f.auth.changePassword(ADMIN_PASSWORD.toCharArray(), "brand-new-password".toCharArray());
    f.auth.signOut();

    assertThat(login("admin", ADMIN_PASSWORD)).isInstanceOf(LoginResult.Failure.class);
    assertThat(login("admin", "brand-new-password")).isInstanceOf(LoginResult.Success.class);
    assertThat(f.count(AuditAction.PASSWORD_CHANGED)).isEqualTo(1);
  }

  @Test
  void temporaryPasswordMustBeChangedAtFirstSignIn() {
    f.admin();
    CreatedUser clerk = f.userService.create("clerk", "Clerk", Role.CLERK);
    f.auth.signOut();

    LoginResult result = login("clerk", clerk.temporaryPassword());
    assertThat(result)
        .isInstanceOfSatisfying(
            LoginResult.Success.class, s -> assertThat(s.mustChangePassword()).isTrue());

    f.auth.changePassword(
        clerk.temporaryPassword().toCharArray(), "clerk-own-password".toCharArray());
    assertThat(f.ctx.session.requireUser().mustChangePassword()).isFalse();
    assertThat(f.users.findById(clerk.user().id()).orElseThrow().mustChangePassword()).isFalse();
  }

  @Test
  void lockScreenFailuresCountTowardLockout() {
    f.admin();
    assertThat(f.auth.unlock(ADMIN_PASSWORD.toCharArray())).isEqualTo(UnlockResult.UNLOCKED);
    for (int i = 0; i < 4; i++) {
      assertThat(f.auth.unlock("wrong-password".toCharArray()))
          .isEqualTo(UnlockResult.WRONG_PASSWORD);
    }
    assertThat(f.auth.unlock(ADMIN_PASSWORD.toCharArray())).isEqualTo(UnlockResult.UNLOCKED);
    assertThat(
            f.users.findById(f.ctx.session.requireUser().id()).orElseThrow().failedLoginAttempts())
        .isZero();
  }

  @Test
  void lockoutFromTheLockScreenEndsTheSession() {
    User admin = f.admin();
    for (int i = 0; i < 4; i++) {
      assertThat(f.auth.unlock("wrong-password".toCharArray()))
          .isEqualTo(UnlockResult.WRONG_PASSWORD);
    }
    assertThat(f.auth.unlock("wrong-password".toCharArray())).isEqualTo(UnlockResult.SESSION_ENDED);

    assertThat(f.ctx.session.currentUser()).isEmpty();
    assertThat(f.count(AuditAction.ACCOUNT_LOCKED)).isEqualTo(1);
    assertThat(f.count(AuditAction.SIGNED_OUT)).isEqualTo(1);
    // The right password doesn't get them back in while the lock lasts; sign-in fails generically.
    assertThat(login(admin.username().value(), ADMIN_PASSWORD))
        .isEqualTo(new LoginResult.Failure(LoginResult.FAILURE_MESSAGE));
  }

  @Test
  void lockScreenEndsTheSessionOfADeactivatedAccount() {
    f.admin();
    User clerk = f.user("clerk", Role.CLERK, "clerk-password-1");
    f.userService.deactivate(clerk.id());
    f.auth.signOut();
    // Simulate a clerk who was signed in before an admin deactivated them.
    f.ctx.session.signIn(clerk);
    assertThat(f.auth.unlock("clerk-password-1".toCharArray()))
        .isEqualTo(UnlockResult.SESSION_ENDED);
    assertThat(f.ctx.session.currentUser()).isEmpty();
  }

  @Test
  void signOutWithoutASessionIsHarmless() {
    f.auth.signOut();
    assertThat(f.ctx.auditEntries()).isEmpty();
  }
}
