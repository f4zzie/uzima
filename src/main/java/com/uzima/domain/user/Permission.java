package com.uzima.domain.user;

/** A capability checked by services before they act. Roles map to permissions in {@link Role}. */
public enum Permission {
  MANAGE_USERS("manage users"),
  EDIT_SETTINGS("edit settings"),
  EDIT_CATALOG("edit catalog prices"),
  EDIT_CLIENTS("create or edit clients"),
  DEACTIVATE_CLIENTS("deactivate clients"),
  CHANGE_CLIENT_CATEGORY("change a client's category"),
  CREATE_JOBS("create quotes and jobs"),
  ADVANCE_JOB_STATUS("advance job status"),
  CANCEL_JOB("cancel jobs"),
  REOPEN_JOB("reopen cancelled jobs"),
  RECORD_PAYMENTS("record payments"),
  REVERSE_PAYMENTS("reverse payments"),
  VIEW_REPORTS("view reports"),
  VIEW_AUDIT_LOG("view the audit log"),
  BACKUP_DATABASE("back up the database"),
  LOAD_DEMO_DATA("load demo data");

  private final String description;

  Permission(String description) {
    this.description = description;
  }

  /** Lower-case verb phrase completing "cannot ...", e.g. "cancel jobs". */
  public String description() {
    return description;
  }
}
