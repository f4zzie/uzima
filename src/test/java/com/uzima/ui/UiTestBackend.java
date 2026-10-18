package com.uzima.ui;

import com.uzima.app.CompositionRoot;
import com.uzima.app.Services;
import com.uzima.domain.common.DisplayFormats;
import com.uzima.persistence.Database;
import com.uzima.persistence.TestDatabases;
import java.time.Clock;

/** A real backend over a fresh in-memory H2 database, for UI tests. */
public final class UiTestBackend implements AutoCloseable {

  public static final String ADMIN_USERNAME = "admin";
  public static final String ADMIN_PASSWORD = "Admin-pass-2026";

  private final Database database;
  public final Services services;
  public final Backend backend;

  public UiTestBackend() {
    database = TestDatabases.h2();
    services = CompositionRoot.wire(database, Clock.system(DisplayFormats.ZONE));
    backend = new Backend(services);
  }

  /** Creates the first administrator (which signs them in), then signs out. */
  public UiTestBackend withAdministrator() {
    services
        .auth()
        .createAdministrator(ADMIN_USERNAME, "Amina Odhiambo", ADMIN_PASSWORD.toCharArray());
    services.auth().signOut();
    return this;
  }

  /** Creates the administrator and leaves them signed in. */
  public UiTestBackend signedInAsAdministrator() {
    services
        .auth()
        .createAdministrator(ADMIN_USERNAME, "Amina Odhiambo", ADMIN_PASSWORD.toCharArray());
    return this;
  }

  @Override
  public void close() {
    backend.close();
    database.close();
  }
}
