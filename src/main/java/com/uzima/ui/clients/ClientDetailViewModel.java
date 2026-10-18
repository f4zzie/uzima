package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.user.Permission;
import com.uzima.service.JobSearch;
import com.uzima.service.JobSummary;
import com.uzima.ui.Backend;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** One client with their jobs and totals, loaded together in the background. */
public final class ClientDetailViewModel {

  private static final Logger log = LoggerFactory.getLogger(ClientDetailViewModel.class);

  static final String LOAD_FAILED =
      "Uzima couldn't load this client. Check the database connection and try again.";

  /** The client, their jobs (newest first) and the totals across them. */
  public record Detail(Client client, List<JobSummary> jobs, ClientTotals totals) {}

  /** What the page is showing. */
  public enum State {
    LOADING,
    READY,
    ERROR
  }

  private final Backend backend;
  private final long clientId;
  private final ReadOnlyObjectWrapper<State> state =
      new ReadOnlyObjectWrapper<>(this, "state", State.LOADING);
  private final ReadOnlyObjectWrapper<Detail> detail = new ReadOnlyObjectWrapper<>(this, "detail");
  private final ReadOnlyStringWrapper error = new ReadOnlyStringWrapper(this, "error", "");

  public ClientDetailViewModel(Backend backend, long clientId) {
    this.backend = backend;
    this.clientId = clientId;
  }

  public void load() {
    state.set(State.LOADING);
    backend.run(
        s -> {
          Client client =
              s.clients()
                  .find(clientId)
                  .orElseThrow(() -> new DomainException("This client no longer exists."));
          List<JobSummary> jobs = s.jobs().search(JobSearch.all().withClient(clientId));
          return new Detail(client, jobs, ClientTotals.of(jobs));
        },
        loaded -> {
          detail.set(loaded);
          state.set(State.READY);
        },
        e -> {
          if (e instanceof DomainException rule) {
            error.set(rule.getMessage());
          } else {
            log.error("Loading client {} failed", clientId, e);
            error.set(LOAD_FAILED);
          }
          state.set(State.ERROR);
        });
  }

  /** Deactivates or reactivates the client, then reloads; {@code onDone} gets the saved client. */
  public void setActive(boolean active, Consumer<Client> onDone, Consumer<Throwable> onFailed) {
    backend.run(
        s -> active ? s.clients().reactivate(clientId) : s.clients().deactivate(clientId),
        saved -> {
          onDone.accept(saved);
          load();
        },
        onFailed);
  }

  public boolean canEdit() {
    return backend.session().has(Permission.EDIT_CLIENTS);
  }

  public boolean canDeactivate() {
    return backend.session().has(Permission.DEACTIVATE_CLIENTS);
  }

  public boolean canCreateJobs() {
    return backend.session().has(Permission.CREATE_JOBS);
  }

  public long clientId() {
    return clientId;
  }

  public ReadOnlyObjectProperty<State> stateProperty() {
    return state.getReadOnlyProperty();
  }

  public ReadOnlyObjectProperty<Detail> detailProperty() {
    return detail.getReadOnlyProperty();
  }

  public ReadOnlyStringProperty errorProperty() {
    return error.getReadOnlyProperty();
  }
}
