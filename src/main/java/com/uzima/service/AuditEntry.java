package com.uzima.service;

import java.time.Instant;
import java.util.Objects;

/**
 * One audit log row. {@code userId} is null when no account is involved (e.g. a failed login with
 * an unknown username); {@code username} always says who acted or who was attempted.
 */
public record AuditEntry(
    long id,
    Instant occurredAt,
    Long userId,
    String username,
    AuditAction action,
    String entityType,
    String entityId,
    String details) {

  public static final int MAX_DETAILS = 4000;

  public AuditEntry {
    Objects.requireNonNull(occurredAt, "occurredAt");
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(action, "action");
    entityType = entityType == null ? "" : entityType;
    entityId = entityId == null ? "" : entityId;
    details = details == null ? "" : details;
    if (details.length() > MAX_DETAILS) {
      details = details.substring(0, MAX_DETAILS - 1) + "…";
    }
  }
}
