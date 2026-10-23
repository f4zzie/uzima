package com.uzima.ui.auth;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.ui.Backend;
import java.util.Arrays;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changing the signed-in user's own password, for both the forced screen after signing in with a
 * temporary password and the voluntary dialog. The confirmation check is local; every other rule
 * comes back from {@code AuthService.changePassword} as a field error.
 */
public final class ChangePasswordViewModel {

  private static final Logger log = LoggerFactory.getLogger(ChangePasswordViewModel.class);

  static final String SAVE_FAILED =
      "Uzima couldn't change the password just now. Try again; if it keeps happening, the details"
          + " are in the log folder.";

  private final Backend backend;
  private final Runnable onChanged;

  private final StringProperty current = new SimpleStringProperty(this, "current", "");
  private final StringProperty password = new SimpleStringProperty(this, "password", "");
  private final StringProperty confirm = new SimpleStringProperty(this, "confirm", "");
  private final StringProperty currentError = new SimpleStringProperty(this, "currentError", "");
  private final StringProperty passwordError = new SimpleStringProperty(this, "pwError", "");
  private final StringProperty confirmError = new SimpleStringProperty(this, "confirmError", "");
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");

  private final Map<String, StringProperty> errorsByField =
      Map.of(
          "currentPassword", currentError,
          "password", passwordError,
          "confirm", confirmError);

  /** {@code onChanged} runs on the FX thread after the password was changed. */
  public ChangePasswordViewModel(Backend backend, Runnable onChanged) {
    this.backend = backend;
    this.onChanged = onChanged;
  }

  public void change() {
    if (busy.get()) {
      return;
    }
    errorsByField.values().forEach(e -> e.set(""));
    failure.set("");
    boolean blank = false;
    if (current.get().isEmpty()) {
      currentError.set("Enter your current password");
      blank = true;
    }
    if (password.get().isEmpty()) {
      passwordError.set("Choose a new password");
      blank = true;
    } else if (!password.get().equals(confirm.get())) {
      confirmError.set("Passwords don't match");
      blank = true;
    }
    if (blank) {
      return;
    }
    busy.set(true);
    char[] old = current.get().toCharArray();
    char[] chosen = password.get().toCharArray();
    backend.run(
        s -> {
          try {
            s.auth().changePassword(old, chosen);
            return Boolean.TRUE;
          } finally {
            Arrays.fill(old, '\0');
            Arrays.fill(chosen, '\0');
          }
        },
        done -> {
          current.set("");
          password.set("");
          confirm.set("");
          busy.set(false);
          onChanged.run();
        },
        this::fail);
  }

  /**
   * Ends the session instead of changing the password (the forced screen's way out), then runs
   * {@code onSignedOut} on the FX thread even if writing the audit entry failed.
   */
  public void signOut(Runnable onSignedOut) {
    if (busy.get()) {
      return;
    }
    busy.set(true);
    backend.run(
        s -> {
          s.auth().signOut();
          return Boolean.TRUE;
        },
        done -> finishSignOut(onSignedOut),
        error -> {
          log.error("Sign-out from the change-password screen failed", error);
          finishSignOut(onSignedOut);
        });
  }

  private void finishSignOut(Runnable onSignedOut) {
    current.set("");
    password.set("");
    confirm.set("");
    busy.set(false);
    onSignedOut.run();
  }

  /** Sets the messages first, then clears busy, so listeners on busy see the final errors. */
  private void fail(Throwable error) {
    switch (error) {
      case ValidationException invalid ->
          invalid
              .errors()
              .forEach(
                  e -> {
                    StringProperty target = errorsByField.get(e.field());
                    if (target != null) {
                      target.set(e.message());
                    } else {
                      failure.set(e.message());
                    }
                  });
      case DomainException rule -> failure.set(rule.getMessage());
      default -> {
        log.error("Changing the password failed", error);
        failure.set(SAVE_FAILED);
      }
    }
    if (!currentError.get().isEmpty()) {
      current.set("");
    }
    busy.set(false);
  }

  public StringProperty currentProperty() {
    return current;
  }

  public StringProperty passwordProperty() {
    return password;
  }

  public StringProperty confirmProperty() {
    return confirm;
  }

  public StringProperty currentErrorProperty() {
    return currentError;
  }

  public StringProperty passwordErrorProperty() {
    return passwordError;
  }

  public StringProperty confirmErrorProperty() {
    return confirmError;
  }

  public StringProperty failureProperty() {
    return failure;
  }

  public BooleanProperty busyProperty() {
    return busy;
  }
}
