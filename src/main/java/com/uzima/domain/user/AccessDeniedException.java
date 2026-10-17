package com.uzima.domain.user;

import com.uzima.domain.common.DomainException;

/** The current user's role lacks a permission that the requested action needs. */
public final class AccessDeniedException extends DomainException {

  private final Role role;
  private final Permission permission;

  public AccessDeniedException(Role role, Permission permission) {
    super("Your role (" + role.label() + ") cannot " + permission.description() + ".");
    this.role = role;
    this.permission = permission;
  }

  public Role role() {
    return role;
  }

  public Permission permission() {
    return permission;
  }
}
