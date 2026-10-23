package com.uzima.ui.auth;

import com.uzima.domain.user.User;
import com.uzima.service.UnlockResult;
import com.uzima.ui.Backend;
import java.util.Arrays;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The idle lock screen. The password is checked by {@code AuthService.unlock}, whose wrong attempts
 * count toward the normal lockout; when the account locks (or was deactivated meanwhile) the
 * service has already ended the session and the app returns to sign-in.
 */
public final class LockViewModel {

  private static final Logger log = LoggerFactory.getLogger(LockViewModel.class);

  static final String WRONG_PASSWORD =
      "Wrong password. "
          + User.MAX_FAILED_ATTEMPTS
          + " wrong attempts in a row sign you out and lock the account for "
          + User.LOCK_DURATION.toMinutes()
          + " minutes.";
  static final String CHECK_FAILED =
      "Uzima couldn't check the password just now. Try again; if it keeps happening, sign out.";

  private final Backend backend;
  private final Runnable onUnlocked;
  private final Runnable onSessionEnded;
  private final Runnable onSignedOut;

  private final StringProperty password = new SimpleStringProperty(this, "password", "");
  private final StringProperty passwordError = new SimpleStringProperty(this, "pwError", "");
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");

  /**
   * @param onUnlocked the password was right; return to where the user was
   * @param onSessionEnded the account is now locked or inactive and the session is over; show
   *     sign-in with the generic message
   * @param onSignedOut the user chose to sign out instead
   */
  public LockViewModel(
      Backend backend, Runnable onUnlocked, Runnable onSessionEnded, Runnable onSignedOut) {
    this.backend = backend;
    this.onUnlocked = onUnlocked;
    this.onSessionEnded = onSessionEnded;
    this.onSignedOut = onSignedOut;
  }

  /** The signed-in user's display name, shown on the lock screen. */
  public String displayName() {
    return backend.session().currentUser().map(User::displayName).orElse("");
  }

  public void unlock() {
    if (busy.get()) {
      return;
    }
    passwordError.set("");
    failure.set("");
    if (password.get().isEmpty()) {
      passwordError.set("Enter your password");
      return;
    }
    busy.set(true);
    char[] secret = password.get().toCharArray();
    backend.run(
        s -> {
          try {
            return s.auth().unlock(secret);
          } finally {
            Arrays.fill(secret, '\0');
          }
        },
        this::answered,
        error -> {
          log.error("Checking the lock-screen password failed", error);
          failure.set(CHECK_FAILED);
          busy.set(false);
        });
  }

  public void signOut() {
    if (busy.get()) {
      return;
    }
    busy.set(true);
    backend.run(
        s -> {
          s.auth().signOut();
          return Boolean.TRUE;
        },
        done -> finishSignOut(),
        error -> {
          log.error("Sign-out from the lock screen failed", error);
          finishSignOut();
        });
  }

  private void finishSignOut() {
    password.set("");
    busy.set(false);
    onSignedOut.run();
  }

  private void answered(UnlockResult result) {
    password.set("");
    switch (result) {
      case UNLOCKED -> {
        busy.set(false);
        onUnlocked.run();
      }
      case WRONG_PASSWORD -> {
        passwordError.set(WRONG_PASSWORD);
        busy.set(false);
      }
      case SESSION_ENDED -> {
        busy.set(false);
        onSessionEnded.run();
      }
    }
  }

  public StringProperty passwordProperty() {
    return password;
  }

  public StringProperty passwordErrorProperty() {
    return passwordError;
  }

  public StringProperty failureProperty() {
    return failure;
  }

  public BooleanProperty busyProperty() {
    return busy;
  }
}
