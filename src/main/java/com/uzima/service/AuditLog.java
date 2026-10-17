package com.uzima.service;

import com.uzima.domain.user.Permission;
import com.uzima.domain.user.User;
import java.time.Clock;
import java.util.List;

/**
 * Writes audit entries for the signed-in user. Called inside the service's transaction, so an entry
 * is saved exactly when the change it describes is saved.
 */
public final class AuditLog {

  private final AuditRepository repository;
  private final Session session;
  private final Clock clock;

  public AuditLog(AuditRepository repository, Session session, Clock clock) {
    this.repository = repository;
    this.session = session;
    this.clock = clock;
  }

  /** Records an action by the signed-in user. */
  public void record(AuditAction action, String entityType, Object entityId, String details) {
    User user = session.requireUser();
    write(user.id(), user.username().value(), action, entityType, entityId, details);
  }

  /** Records an action attributed to {@code user}, e.g. a login before the session is set. */
  public void recordFor(
      User user, AuditAction action, String entityType, Object entityId, String details) {
    write(user.id(), user.username().value(), action, entityType, entityId, details);
  }

  /** Records an action with no matching account, e.g. a failed login for an unknown username. */
  public void recordAnonymous(String attemptedUsername, AuditAction action, String details) {
    write(null, attemptedUsername, action, "", "", details);
  }

  /** Newest entries matching {@code query}. ADMIN only. */
  public List<AuditEntry> search(AuditQuery query) {
    session.require(Permission.VIEW_AUDIT_LOG);
    return repository.search(query);
  }

  private void write(
      Long userId,
      String username,
      AuditAction action,
      String entityType,
      Object entityId,
      String details) {
    String safeName = username == null ? "" : username.strip();
    if (safeName.length() > 64) {
      safeName = safeName.substring(0, 64);
    }
    repository.append(
        new AuditEntry(
            0,
            clock.instant(),
            userId,
            safeName,
            action,
            entityType,
            entityId == null ? "" : entityId.toString(),
            details));
  }
}
