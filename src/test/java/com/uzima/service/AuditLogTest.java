package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuditLogTest {

  private final ServiceTestContext ctx = ServiceTestContext.h2();

  @AfterEach
  void close() {
    ctx.close();
  }

  @Test
  void recordsTheSignedInUserAndClockTime() {
    User manager = ctx.signInAs(Role.MANAGER);

    ctx.audit.record(AuditAction.CLIENT_CREATED, "client", "UZM-DOM-0001", "name: Wanjiru");

    AuditEntry e = ctx.auditEntries().getFirst();
    assertThat(e.userId()).isEqualTo(manager.id());
    assertThat(e.username()).isEqualTo(manager.username().value());
    assertThat(e.occurredAt()).isEqualTo(ServiceTestContext.START);
    assertThat(e.entityId()).isEqualTo("UZM-DOM-0001");
    assertThat(e.details()).isEqualTo("name: Wanjiru");
  }

  @Test
  void recordRequiresASignedInUser() {
    assertThatThrownBy(() -> ctx.audit.record(AuditAction.CLIENT_CREATED, "client", 1, ""))
        .isInstanceOf(NotSignedInException.class);
  }

  @Test
  void anonymousEntriesHaveNoUserIdAndTrimTheAttemptedName() {
    ctx.audit.recordAnonymous("  " + "x".repeat(100) + "  ", AuditAction.LOGIN_FAILED, "Unknown");

    AuditEntry e = ctx.auditEntries().getFirst();
    assertThat(e.userId()).isNull();
    assertThat(e.username()).hasSize(64);
    assertThat(e.entityType()).isEmpty();
  }

  @Test
  void longDetailsAreTruncated() {
    ctx.signInAs(Role.ADMIN);
    ctx.audit.record(AuditAction.SETTINGS_CHANGED, "settings", null, "d".repeat(5000));

    AuditEntry e = ctx.auditEntries().getFirst();
    assertThat(e.details()).hasSize(AuditEntry.MAX_DETAILS).endsWith("…");
    assertThat(e.entityId()).isEmpty();
  }

  @Test
  void searchIsForAdminsOnly() {
    ctx.signInAs(Role.MANAGER);
    assertThatThrownBy(() -> ctx.audit.search(AuditQuery.latest(10)))
        .isInstanceOf(AccessDeniedException.class);

    ctx.signInAs(Role.ADMIN);
    ctx.audit.record(AuditAction.SETTINGS_CHANGED, "settings", "", "x");
    assertThat(ctx.audit.search(AuditQuery.latest(10))).hasSize(1);
  }
}
