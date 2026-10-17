package com.uzima.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoleTest {

  /** The permissions matrix from the spec, written out independently of {@link Role}. */
  private static final Set<Permission> MANAGER =
      EnumSet.of(
          Permission.EDIT_CATALOG,
          Permission.EDIT_CLIENTS,
          Permission.DEACTIVATE_CLIENTS,
          Permission.CREATE_JOBS,
          Permission.ADVANCE_JOB_STATUS,
          Permission.CANCEL_JOB,
          Permission.RECORD_PAYMENTS,
          Permission.REVERSE_PAYMENTS,
          Permission.VIEW_REPORTS);

  private static final Set<Permission> CLERK =
      EnumSet.of(
          Permission.EDIT_CLIENTS,
          Permission.CREATE_JOBS,
          Permission.ADVANCE_JOB_STATUS,
          Permission.RECORD_PAYMENTS);

  private static boolean expected(Role role, Permission permission) {
    return switch (role) {
      case ADMIN -> true;
      case MANAGER -> MANAGER.contains(permission);
      case CLERK -> CLERK.contains(permission);
    };
  }

  static Stream<Arguments> everyRoleAndPermission() {
    List<Arguments> args = new ArrayList<>();
    for (Role role : Role.values()) {
      for (Permission permission : Permission.values()) {
        args.add(Arguments.of(role, permission, expected(role, permission)));
      }
    }
    return args.stream();
  }

  @ParameterizedTest(name = "{0} has {1}: {2}")
  @MethodSource("everyRoleAndPermission")
  void matrixMatchesSpec(Role role, Permission permission, boolean allowed) {
    assertThat(role.has(permission)).isEqualTo(allowed);
    assertThat(role.permissions().contains(permission)).isEqualTo(allowed);
    if (allowed) {
      assertThatCode(() -> role.require(permission)).doesNotThrowAnyException();
    } else {
      assertThatThrownBy(() -> role.require(permission)).isInstanceOf(AccessDeniedException.class);
    }
  }

  @Test
  void adminOnlyPermissions() {
    for (Permission p :
        EnumSet.of(
            Permission.MANAGE_USERS,
            Permission.EDIT_SETTINGS,
            Permission.REOPEN_JOB,
            Permission.VIEW_AUDIT_LOG,
            Permission.BACKUP_DATABASE,
            Permission.LOAD_DEMO_DATA)) {
      assertThat(Role.ADMIN.has(p)).as(p.name()).isTrue();
      assertThat(Role.MANAGER.has(p)).as(p.name()).isFalse();
      assertThat(Role.CLERK.has(p)).as(p.name()).isFalse();
    }
  }

  @Test
  void permissionSetsAreUnmodifiable() {
    assertThatThrownBy(() -> Role.CLERK.permissions().add(Permission.CANCEL_JOB))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void accessDeniedMessageNamesRoleAndAction() {
    AccessDeniedException e = new AccessDeniedException(Role.CLERK, Permission.CANCEL_JOB);
    assertThat(e).hasMessage("Your role (Clerk) cannot cancel jobs.");
    assertThat(e.role()).isEqualTo(Role.CLERK);
    assertThat(e.permission()).isEqualTo(Permission.CANCEL_JOB);
  }

  @Test
  void everyPermissionHasDescription() {
    for (Permission p : Permission.values()) {
      assertThat(p.description()).isNotBlank();
    }
  }
}
