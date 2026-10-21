package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.user.Permission;
import com.uzima.ui.Backend;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moves a client to another category (MANAGER and above). The client ID keeps its prefix, saved
 * jobs keep their quoted prices, and the service writes an "old → new" audit entry.
 */
public final class CategoryChangeViewModel {

  private static final Logger log = LoggerFactory.getLogger(CategoryChangeViewModel.class);

  private final Backend backend;
  private final Client client;
  private final ObjectProperty<ClientCategory> target = new SimpleObjectProperty<>(this, "target");
  private final StringProperty error = new SimpleStringProperty(this, "error", "");
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");

  public CategoryChangeViewModel(Backend backend, Client client) {
    this.backend = backend;
    this.client = client;
    target.set(choices().get(0));
  }

  /** Whether the signed-in user may change categories at all. */
  public static boolean allowed(Backend backend) {
    return backend.session().has(Permission.CHANGE_CLIENT_CATEGORY);
  }

  public Client client() {
    return client;
  }

  /** The categories the client can move to (every one except its current category). */
  public List<ClientCategory> choices() {
    return Arrays.stream(ClientCategory.values()).filter(c -> c != client.category()).toList();
  }

  /** The note shown in the dialog, naming this client's ID. */
  public String auditNote() {
    return "The client ID ("
        + client.code().value()
        + ") stays the same. Saved jobs keep the prices they were quoted with; only new quotes use"
        + " the new category's fees. This change is recorded in the audit log.";
  }

  public void change(Consumer<Client> onChanged) {
    if (busy.get()) {
      return;
    }
    error.set("");
    failure.set("");
    ClientCategory chosen = target.get();
    if (chosen == null) {
      error.set("Choose the new category");
      return;
    }
    busy.set(true);
    backend.run(
        s -> s.clients().changeCategory(client.id(), chosen),
        changed -> {
          busy.set(false);
          onChanged.accept(changed);
        },
        e -> {
          switch (e) {
            case ValidationException invalid ->
                error.set(invalid.messageFor("category").orElse(invalid.getMessage()));
            case DomainException rule -> failure.set(rule.getMessage());
            default -> {
              log.error("Changing a client's category failed", e);
              failure.set(ClientFormViewModel.SAVE_FAILED);
            }
          }
          busy.set(false);
        });
  }

  public ObjectProperty<ClientCategory> targetProperty() {
    return target;
  }

  public StringProperty errorProperty() {
    return error;
  }

  public StringProperty failureProperty() {
    return failure;
  }

  public BooleanProperty busyProperty() {
    return busy;
  }
}
