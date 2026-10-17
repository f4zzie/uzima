package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import org.junit.jupiter.api.Test;

class SessionTest {

  private static User user(long id, Role role) {
    return User.create(new Username("user" + id), "User " + id, role, "hash", false).withRole(role);
  }

  @Test
  void emptySessionRejectsProtectedCalls() {
    Session session = new Session();
    assertThat(session.currentUser()).isEmpty();
    assertThat(session.has(Permission.CREATE_JOBS)).isFalse();
    assertThatThrownBy(session::requireUser).isInstanceOf(NotSignedInException.class);
    assertThatThrownBy(() -> session.require(Permission.CREATE_JOBS))
        .isInstanceOf(NotSignedInException.class);
  }

  @Test
  void requireChecksTheRoleOfTheSignedInUser() {
    Session session = new Session();
    User clerk = user(1, Role.CLERK);
    session.signIn(clerk);

    assertThat(session.require(Permission.CREATE_JOBS)).isEqualTo(clerk);
    assertThat(session.has(Permission.CANCEL_JOB)).isFalse();
    assertThatThrownBy(() -> session.require(Permission.CANCEL_JOB))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("cancel jobs");
  }

  @Test
  void refreshOnlyReplacesTheSameUser() {
    Session session = new Session();
    User clerk =
        new User(
            1,
            new Username("clerk"),
            "Clerk",
            Role.CLERK,
            "h",
            true,
            false,
            0,
            java.util.Optional.empty());
    session.signIn(clerk);

    session.refresh(clerk.withRole(Role.MANAGER));
    assertThat(session.requireUser().role()).isEqualTo(Role.MANAGER);

    User other =
        new User(
            2,
            new Username("other"),
            "Other",
            Role.ADMIN,
            "h",
            true,
            false,
            0,
            java.util.Optional.empty());
    session.refresh(other);
    assertThat(session.requireUser().id()).isEqualTo(1);
  }

  @Test
  void signOutClearsTheUser() {
    Session session = new Session();
    session.signIn(user(1, Role.ADMIN));
    session.signOut();
    assertThat(session.currentUser()).isEmpty();
    session.refresh(user(1, Role.ADMIN));
    assertThat(session.currentUser()).isEmpty();
  }
}
