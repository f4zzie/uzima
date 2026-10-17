package com.uzima.service;

import java.util.List;

/** Append-only store of audit entries. */
public interface AuditRepository {

  /** Saves the entry and returns its id. */
  long append(AuditEntry entry);

  /** Newest first. */
  List<AuditEntry> search(AuditQuery query);
}
