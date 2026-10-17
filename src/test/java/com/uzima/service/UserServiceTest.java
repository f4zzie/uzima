package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

  private final AuthTestFixture f = new AuthTestFixture();

  @AfterEach
  void close() {
    f.close();
  }

  private void signInAs(String username, String password) {
    f.auth.signOut();
    assertThat(f.auth.login(username, password.toCharArray()))
        .isInstanceOf(LoginResult.Success.class);
  }

  @Test
  void createsUserWithTemporaryPasswordThatMustBeChanged() {
    f.admin();
    CreatedUser created = f.userService.create(" Wanjiru ", "Wanjiru K.", Role.MANAGER);

    assertThat(created.user().username().value()).isEqualTo("wanjiru");
    assertThat(created.user().mustChangePassword()).isTrue();
    assertThat(created.temporaryPassword()).hasSize(TemporaryPasswords.LENGTH);
    assertThat(created.toString()).doesNotContain(created.temporaryPassword());
    assertThat(created.user().passwordHash()).doesNotContain(created.temporaryPassword());
    AuditEntry audit = f.ctx.auditEntries().getFirst();
    assertThat(audit.action()).isEqualTo(AuditAction.USER_CREATED);
    assertThat(audit.details()).contains("Manager").doesNotContain(created.temporaryPassword());
  }

  @Test
  void rejectsTakenUsernamesAndInvalidInput() {
    f.admin();
    f.userService.create("otieno", "Otieno", Role.CLERK);

    assertThatThrownBy(() -> f.userService.create("OTIENO", "Another", Role.CLERK))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("username")).isPresent());
    assertThatThrownBy(() -> f.userService.create("x", "", null))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(err -> err.field())
                    .containsExactlyInAnyOrder("username", "displayName", "role"));
  }

  @Test
  void onlyAdminsManageUsers() {
    f.admin();
    f.user("clerk", Role.CLERK, "clerk-password-1");
    f.user("manager", Role.MANAGER, "manager-password-1");

    signInAs("clerk", "clerk-password-1");
    assertThatThrownBy(() -> f.userService.create("new", "New", Role.CLERK))
        .isInstanceOf(AccessDeniedException.class);

    signInAs("manager", "manager-password-1");
    assertThatThrownBy(() -> f.userService.create("new", "New", Role.CLERK))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(f.userService::list).isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> f.userService.resetPassword(1))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void changesRoleAndAuditsOldToNew() {
    f.admin();
    User clerk = f.user("clerk", Role.CLERK, "clerk-password-1");

    User promoted = f.userService.changeRole(clerk.id(), Role.MANAGER);

    assertThat(promoted.role()).isEqualTo(Role.MANAGER);
    assertThat(f.ctx.auditEntries().getFirst().details()).isEqualTo("role: Clerk → Manager");
    assertThat(f.userService.changeRole(clerk.id(), Role.MANAGER)).isEqualTo(promoted);
    assertThat(f.count(AuditAction.USER_ROLE_CHANGED)).isEqualTo(1);
  }

  @Test
  void lastActiveAdministratorCannotBeDemoted() {
    User admin = f.admin();
    assertThatThrownBy(() -> f.userService.changeRole(admin.id(), Role.MANAGER))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("at least one active administrator");

    f.user("second", Role.ADMIN, "second-password");
    User demoted = f.userService.changeRole(admin.id(), Role.MANAGER);
    assertThat(demoted.role()).isEqualTo(Role.MANAGER);
    assertThat(f.ctx.session.requireUser().role()).isEqualTo(Role.MANAGER);
  }

  @Test
  void cannotDeactivateYourselfOrTheLastAdmin() {
    User admin = f.admin();
    assertThatThrownBy(() -> f.userService.deactivate(admin.id()))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("your own");

    User second = f.user("second", Role.ADMIN, "second-password");
    signInAs("second", "second-password");
    f.userService.deactivate(admin.id());
    assertThat(f.users.countActiveAdmins()).isEqualTo(1);
    assertThatThrownBy(() -> f.userService.deactivate(second.id()))
        .isInstanceOf(DomainException.class);
  }

  @Test
  void deactivatedUsersCannotSignInUntilReactivated() {
    f.admin();
    User clerk = f.user("clerk", Role.CLERK, "clerk-password-1");

    f.userService.deactivate(clerk.id());
    assertThat(f.userService.deactivate(clerk.id()).active()).isFalse();
    assertThat(f.auth.login("clerk", "clerk-password-1".toCharArray()))
        .isInstanceOf(LoginResult.Failure.class);

    f.auth.login("admin", AuthTestFixture.ADMIN_PASSWORD.toCharArray());
    f.userService.reactivate(clerk.id());
    assertThat(f.userService.reactivate(clerk.id()).active()).isTrue();
    assertThat(f.count(AuditAction.USER_DEACTIVATED)).isEqualTo(1);
    assertThat(f.count(AuditAction.USER_REACTIVATED)).isEqualTo(1);
    assertThat(f.auth.login("clerk", "clerk-password-1".toCharArray()))
        .isInstanceOf(LoginResult.Success.class);
  }

  @Test
  void resetPasswordClearsLockoutAndForcesChange() {
    f.admin();
    User clerk = f.user("clerk", Role.CLERK, "clerk-password-1");
    for (int i = 0; i < 5; i++) {
      f.auth.login("clerk", "wrong-password".toCharArray());
    }
    f.auth.login("admin", AuthTestFixture.ADMIN_PASSWORD.toCharArray());

    String temporary = f.userService.resetPassword(clerk.id());

    User reset = f.users.findById(clerk.id()).orElseThrow();
    assertThat(reset.lockedUntil()).isEmpty();
    assertThat(reset.mustChangePassword()).isTrue();
    assertThat(f.count(AuditAction.PASSWORD_RESET)).isEqualTo(1);
    assertThat(f.auth.login("clerk", temporary.toCharArray()))
        .isInstanceOfSatisfying(
            LoginResult.Success.class, s -> assertThat(s.mustChangePassword()).isTrue());
  }

  @Test
  void listShowsAllUsers() {
    f.admin();
    f.user("clerk", Role.CLERK, "clerk-password-1");
    assertThat(f.userService.list())
        .extracting(u -> u.username().value())
        .containsExactly("admin", "clerk");
  }

  @Test
  void missingUserIsReported() {
    f.admin();
    assertThatThrownBy(() -> f.userService.reactivate(999)).isInstanceOf(DomainException.class);
  }

  @Test
  void themeIsAPersonalPreference() {
    f.admin();
    f.user("clerk", Role.CLERK, "clerk-password-1");
    assertThat(f.userService.theme()).isEqualTo(Theme.LIGHT);
    f.userService.setTheme(Theme.DARK);
    assertThat(f.userService.theme()).isEqualTo(Theme.DARK);

    signInAs("clerk", "clerk-password-1");
    assertThat(f.userService.theme()).isEqualTo(Theme.LIGHT);
    f.userService.setTheme(Theme.DARK);
    assertThat(f.userService.theme()).isEqualTo(Theme.DARK);
  }
}
