package com.uzima.ui.clients;

import com.uzima.ui.Backend;
import com.uzima.ui.Pages;
import com.uzima.ui.shell.Navigator;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/**
 * A {@link Pages} for client tests: {@link #client} pushes the real detail page, as the app does;
 * everything else is only recorded.
 */
final class ClientsTestPages implements Pages {

  final List<String> calls = new ArrayList<>();
  private final Backend backend;
  private Navigator nav;

  ClientsTestPages(Backend backend) {
    this.backend = backend;
  }

  /** The navigator of the shell the clients page was opened in. */
  ClientsTestPages attach(Navigator navigator) {
    this.nav = navigator;
    return this;
  }

  @Override
  public void clients() {
    calls.add("clients");
    nav.open("clients");
  }

  @Override
  public void client(long clientId) {
    calls.add("client " + clientId);
    ClientDetailPage.show(backend, nav, this, clientId);
  }

  @Override
  public void jobs() {
    calls.add("jobs");
  }

  @Override
  public void job(long jobId) {
    calls.add("job " + jobId);
  }

  @Override
  public void newJob(OptionalLong clientId) {
    calls.add("newJob " + (clientId.isPresent() ? clientId.getAsLong() : "-"));
  }

  @Override
  public void editJob(long jobId) {
    calls.add("editJob " + jobId);
  }
}
