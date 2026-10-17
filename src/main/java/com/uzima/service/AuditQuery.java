package com.uzima.service;

import java.time.Instant;
import java.util.Optional;

/** Filters for the audit log screen. Empty values match everything. */
public record AuditQuery(
    Optional<Instant> from,
    Optional<Instant> to,
    Optional<AuditAction> action,
    Optional<Long> userId,
    Optional<String> text,
    int limit) {

  public static AuditQuery latest(int limit) {
    return new AuditQuery(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        limit);
  }
}
