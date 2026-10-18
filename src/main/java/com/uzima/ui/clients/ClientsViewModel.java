package com.uzima.ui.clients;

import com.uzima.service.ClientSearch;
import com.uzima.service.ClientSummary;
import com.uzima.ui.Backend;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The client list: loads summaries in the background, with or without inactive clients. */
public final class ClientsViewModel {

  private static final Logger log = LoggerFactory.getLogger(ClientsViewModel.class);

  static final String LOAD_FAILED =
      "Uzima couldn't load the clients. Check the database connection and try again.";

  /** What the list is showing. */
  public enum State {
    LOADING,
    READY,
    ERROR
  }

  private final Backend backend;
  private final ObservableList<ClientSummary> rows = FXCollections.observableArrayList();
  private final ReadOnlyObjectWrapper<State> state =
      new ReadOnlyObjectWrapper<>(this, "state", State.LOADING);
  private final ObjectProperty<String> error = new SimpleObjectProperty<>(this, "error", "");
  private final BooleanProperty showInactive = new SimpleBooleanProperty(this, "showInactive");
  private long generation;

  public ClientsViewModel(Backend backend) {
    this.backend = backend;
    showInactive.addListener((obs, was, now) -> load());
  }

  /** Reloads the list; a slower earlier load that finishes later is ignored. */
  public void load() {
    long mine = ++generation;
    state.set(State.LOADING);
    ClientSearch search =
        showInactive.get() ? ClientSearch.all() : ClientSearch.all().withActive(true);
    backend.run(
        s -> s.clients().search(search),
        found -> {
          if (mine == generation) {
            rows.setAll(found);
            state.set(State.READY);
          }
        },
        e -> {
          if (mine == generation) {
            log.error("Loading clients failed", e);
            error.set(LOAD_FAILED);
            state.set(State.ERROR);
          }
        });
  }

  public ObservableList<ClientSummary> rows() {
    return rows;
  }

  public List<ClientSummary> snapshot() {
    return List.copyOf(rows);
  }

  public ReadOnlyObjectProperty<State> stateProperty() {
    return state.getReadOnlyProperty();
  }

  public String error() {
    return error.get();
  }

  public BooleanProperty showInactiveProperty() {
    return showInactive;
  }
}
