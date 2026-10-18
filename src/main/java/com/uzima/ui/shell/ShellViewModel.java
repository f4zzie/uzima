package com.uzima.ui.shell;

import com.uzima.domain.user.User;
import com.uzima.ui.Backend;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** State behind the app shell: who is signed in, which pages they may open, and which is open. */
public final class ShellViewModel {

  private static final Logger log = LoggerFactory.getLogger(ShellViewModel.class);

  private final Backend backend;
  private final User user;
  private final List<Destination> destinations;
  private final ObjectProperty<Destination> selected = new SimpleObjectProperty<>(this, "selected");
  private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
  private final Runnable onSignedOut;
  private final Runnable onLock;

  /**
   * @param onLock shows the lock screen; null hides the "Lock screen" command
   * @param onSignedOut called on the FX thread once the session has ended
   */
  public ShellViewModel(
      Backend backend, User user, List<Destination> all, Runnable onLock, Runnable onSignedOut) {
    this.backend = backend;
    this.user = user;
    this.destinations = all.stream().filter(d -> d.visibleTo(user.role())).toList();
    this.onLock = onLock;
    this.onSignedOut = onSignedOut;
    title.bind(
        selected.map(Destination::title).orElse("Welcome, " + firstName(user.displayName())));
    if (!destinations.isEmpty()) {
      selected.set(destinations.get(0));
    }
  }

  public List<Destination> destinations() {
    return destinations;
  }

  public ObjectProperty<Destination> selectedProperty() {
    return selected;
  }

  public ReadOnlyStringProperty titleProperty() {
    return title.getReadOnlyProperty();
  }

  public String displayName() {
    return user.displayName();
  }

  public String roleLabel() {
    return user.role().label();
  }

  public String initials() {
    String[] parts = user.displayName().trim().split("\\s+");
    String first = parts[0].substring(0, 1);
    String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
    return (first + last).toUpperCase(java.util.Locale.ROOT);
  }

  /** Selects the destination at {@code index} (0-based), if there is one. */
  public boolean select(int index) {
    if (index < 0 || index >= destinations.size()) {
      return false;
    }
    selected.set(destinations.get(index));
    return true;
  }

  public Optional<Runnable> lockAction() {
    return Optional.ofNullable(onLock);
  }

  /** Ends the session in the background, then returns to sign-in even if the audit write failed. */
  public void signOut() {
    backend.run(
        s -> {
          s.auth().signOut();
          return null;
        },
        done -> onSignedOut.run(),
        error -> {
          log.error("Sign-out failed", error);
          onSignedOut.run();
        });
  }

  private static String firstName(String displayName) {
    return displayName.trim().split("\\s+")[0];
  }
}
