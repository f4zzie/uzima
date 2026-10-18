package com.uzima.ui.auth;

import com.uzima.service.LoginResult;
import com.uzima.ui.Backend;
import java.util.Arrays;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sign-in form state. Blank fields are caught here and shown under the field; everything the
 * service rejects is shown as the one generic message, so the screen never reveals which usernames
 * exist.
 */
public final class LoginViewModel {

  private static final Logger log = LoggerFactory.getLogger(LoginViewModel.class);

  static final String UNEXPECTED =
      "Uzima couldn't check your sign-in just now. Try again; if it keeps happening, the details"
          + " are in the log folder.";

  private final Backend backend;
  private final Consumer<LoginResult.Success> onSignedIn;

  private final StringProperty username = new SimpleStringProperty(this, "username", "");
  private final StringProperty password = new SimpleStringProperty(this, "password", "");
  private final StringProperty usernameError = new SimpleStringProperty(this, "usernameError", "");
  private final StringProperty passwordError = new SimpleStringProperty(this, "passwordError", "");
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");

  /**
   * @param initialMessage shown in the banner when the screen opens (e.g. after the lock screen
   *     ended the session); null or blank for none
   */
  public LoginViewModel(
      Backend backend, Consumer<LoginResult.Success> onSignedIn, String initialMessage) {
    this.backend = backend;
    this.onSignedIn = onSignedIn;
    failure.set(initialMessage == null ? "" : initialMessage);
  }

  public void signIn() {
    if (busy.get()) {
      return;
    }
    usernameError.set(username.get().isBlank() ? "Enter your username" : "");
    passwordError.set(password.get().isEmpty() ? "Enter your password" : "");
    if (!usernameError.get().isEmpty() || !passwordError.get().isEmpty()) {
      return;
    }
    failure.set("");
    busy.set(true);
    String name = username.get();
    char[] secret = password.get().toCharArray();
    backend.run(
        s -> {
          try {
            return s.auth().login(name, secret);
          } finally {
            Arrays.fill(secret, '\0');
          }
        },
        this::handle,
        error -> {
          log.error("Sign-in failed unexpectedly", error);
          busy.set(false);
          failure.set(UNEXPECTED);
        });
  }

  private void handle(LoginResult result) {
    busy.set(false);
    password.set("");
    switch (result) {
      case LoginResult.Success success -> onSignedIn.accept(success);
      case LoginResult.Failure f -> failure.set(f.message());
    }
  }

  public StringProperty usernameProperty() {
    return username;
  }

  public StringProperty passwordProperty() {
    return password;
  }

  public StringProperty usernameErrorProperty() {
    return usernameError;
  }

  public StringProperty passwordErrorProperty() {
    return passwordError;
  }

  /** Form-level message for the banner; empty when there is none. */
  public StringProperty failureProperty() {
    return failure;
  }

  public BooleanProperty busyProperty() {
    return busy;
  }
}
