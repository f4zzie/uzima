package com.uzima.ui.auth;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.user.PasswordPolicy;
import com.uzima.domain.user.User;
import com.uzima.ui.Backend;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First-run form: creates the administrator account. Field errors from the service land under the
 * matching field (by {@code FieldError.field()}); the confirmation check is local.
 */
public final class FirstRunViewModel {

  private static final Logger log = LoggerFactory.getLogger(FirstRunViewModel.class);

  public static final String PASSWORD_HINT =
      "At least "
          + PasswordPolicy.MIN_LENGTH
          + " characters. A short phrase is easiest to remember.";

  static final String SAVE_FAILED =
      "Uzima couldn't save the account just now. Try again; if it keeps happening, the details"
          + " are in the log folder.";

  private final Backend backend;
  private final Consumer<User> onCreated;

  private final StringProperty displayName = new SimpleStringProperty(this, "displayName", "");
  private final StringProperty username = new SimpleStringProperty(this, "username", "");
  private final StringProperty password = new SimpleStringProperty(this, "password", "");
  private final StringProperty confirm = new SimpleStringProperty(this, "confirm", "");
  private final StringProperty displayNameError = new SimpleStringProperty(this, "dnError", "");
  private final StringProperty usernameError = new SimpleStringProperty(this, "userError", "");
  private final StringProperty passwordError = new SimpleStringProperty(this, "pwError", "");
  private final StringProperty confirmError = new SimpleStringProperty(this, "confirmError", "");
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");

  private final Map<String, StringProperty> errorsByField =
      Map.of(
          "displayName", displayNameError,
          "username", usernameError,
          "password", passwordError,
          "confirm", confirmError);

  public FirstRunViewModel(Backend backend, Consumer<User> onCreated) {
    this.backend = backend;
    this.onCreated = onCreated;
  }

  public void create() {
    if (busy.get()) {
      return;
    }
    errorsByField.values().forEach(e -> e.set(""));
    failure.set("");
    if (!password.get().isEmpty() && !password.get().equals(confirm.get())) {
      confirmError.set("Passwords don't match");
      return;
    }
    busy.set(true);
    String name = username.get();
    String display = displayName.get().trim();
    char[] secret = password.get().toCharArray();
    backend.run(
        s -> {
          try {
            return s.auth().createAdministrator(name, display, secret);
          } finally {
            Arrays.fill(secret, '\0');
          }
        },
        user -> {
          busy.set(false);
          password.set("");
          confirm.set("");
          onCreated.accept(user);
        },
        this::fail);
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
        log.error("Creating the administrator failed", error);
        failure.set(SAVE_FAILED);
      }
    }
    busy.set(false);
  }

  public StringProperty displayNameProperty() {
    return displayName;
  }

  public StringProperty usernameProperty() {
    return username;
  }

  public StringProperty passwordProperty() {
    return password;
  }

  public StringProperty confirmProperty() {
    return confirm;
  }

  public StringProperty displayNameErrorProperty() {
    return displayNameError;
  }

  public StringProperty usernameErrorProperty() {
    return usernameError;
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
