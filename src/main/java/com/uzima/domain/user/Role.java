package com.uzima.domain.user;

import static com.uzima.domain.user.Permission.ADVANCE_JOB_STATUS;
import static com.uzima.domain.user.Permission.CANCEL_JOB;
import static com.uzima.domain.user.Permission.CHANGE_CLIENT_CATEGORY;
import static com.uzima.domain.user.Permission.CREATE_JOBS;
import static com.uzima.domain.user.Permission.DEACTIVATE_CLIENTS;
import static com.uzima.domain.user.Permission.EDIT_CATALOG;
import static com.uzima.domain.user.Permission.EDIT_CLIENTS;
import static com.uzima.domain.user.Permission.RECORD_PAYMENTS;
import static com.uzima.domain.user.Permission.REVERSE_PAYMENTS;
import static com.uzima.domain.user.Permission.VIEW_REPORTS;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Staff roles. The role-to-permission matrix below is the single source of truth: services check
 * it, and the UI reads it to hide controls.
 */
public enum Role {
  ADMIN("Admin"),
  MANAGER("Manager"),
  CLERK("Clerk");

  private static final Map<Role, Set<Permission>> PERMISSIONS = new EnumMap<>(Role.class);

  static {
    PERMISSIONS.put(ADMIN, Collections.unmodifiableSet(EnumSet.allOf(Permission.class)));
    PERMISSIONS.put(
        MANAGER,
        Collections.unmodifiableSet(
            EnumSet.of(
                EDIT_CATALOG,
                EDIT_CLIENTS,
                DEACTIVATE_CLIENTS,
                CHANGE_CLIENT_CATEGORY,
                CREATE_JOBS,
                ADVANCE_JOB_STATUS,
                CANCEL_JOB,
                RECORD_PAYMENTS,
                REVERSE_PAYMENTS,
                VIEW_REPORTS)));
    PERMISSIONS.put(
        CLERK,
        Collections.unmodifiableSet(
            EnumSet.of(EDIT_CLIENTS, CREATE_JOBS, ADVANCE_JOB_STATUS, RECORD_PAYMENTS)));
  }

  private final String label;

  Role(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  public Set<Permission> permissions() {
    return PERMISSIONS.get(this);
  }

  public boolean has(Permission permission) {
    return permissions().contains(permission);
  }

  /** Throws {@link AccessDeniedException} unless this role has {@code permission}. */
  public void require(Permission permission) {
    if (!has(permission)) {
      throw new AccessDeniedException(this, permission);
    }
  }
}
