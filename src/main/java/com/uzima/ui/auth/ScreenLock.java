package com.uzima.ui.auth;

import com.uzima.ui.Backend;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Locks the signed-in window: after a period without keyboard or mouse input, or on request ("Lock
 * screen", Ctrl+L). The lock screen is laid over the existing content, which is hidden and disabled
 * rather than rebuilt, so unlocking returns to exactly the same page, detail stack, open dialog and
 * focused field.
 *
 * <p>While locked nothing underneath can be reached: the covered node is invisible and disabled (no
 * mouse, no focus, no keys), and the scene's keyboard accelerators (Ctrl+N, Ctrl+S…) are set aside
 * until unlock. The idle timer stops while locked and restarts on unlock.
 */
public final class ScreenLock {

  private final Backend backend;
  private final StackPane layer;
  private final Node covered;
  private final Runnable onSessionEnded;
  private final Runnable onSignedOut;
  private final PauseTransition idle = new PauseTransition();
  private final ReadOnlyBooleanWrapper locked = new ReadOnlyBooleanWrapper(this, "locked");
  private final EventHandler<InputEvent> activity = e -> restartIdleTimer();
  private final ChangeListener<Scene> sceneListener = (obs, old, now) -> attach(old, now);
  private final Map<KeyCombination, Runnable> setAside = new LinkedHashMap<>();
  private boolean timing;
  private boolean stopped;
  private Node focusBefore;
  private Parent lockRoot;

  /**
   * @param layer the pane the lock screen is added to; {@code covered} must be its child
   * @param covered what the lock screen hides (the window's content and its overlays)
   * @param onSessionEnded the lock screen's wrong attempts locked the account and the session is
   *     over; show sign-in with the generic message
   * @param onSignedOut the user chose "Sign out" on the lock screen
   */
  public ScreenLock(
      Backend backend,
      StackPane layer,
      Node covered,
      Runnable onSessionEnded,
      Runnable onSignedOut) {
    this.backend = Objects.requireNonNull(backend);
    this.layer = layer;
    this.covered = covered;
    this.onSessionEnded = onSessionEnded;
    this.onSignedOut = onSignedOut;
    idle.setOnFinished(e -> lock());
    layer.sceneProperty().addListener(sceneListener);
    attach(null, layer.getScene());
  }

  /** Starts locking after {@code timeout} without input. Does nothing once stopped. */
  public void startIdleTimer(Duration timeout) {
    if (stopped) {
      return;
    }
    idle.setDuration(timeout);
    timing = true;
    restartIdleTimer();
  }

  public Duration idleTimeout() {
    return idle.getDuration();
  }

  /** Shows the lock screen. Calling it while locked, or after {@link #stop}, does nothing. */
  public void lock() {
    if (locked.get() || stopped) {
      return;
    }
    idle.stop();
    Scene scene = layer.getScene();
    focusBefore = scene == null ? null : scene.getFocusOwner();
    if (scene != null) {
      setAside.putAll(scene.getAccelerators());
      scene.getAccelerators().clear();
    }
    LockView view =
        new LockView(
            new LockViewModel(backend, this::unlocked, this::sessionEnded, this::signedOut));
    lockRoot = view.root();
    covered.setDisable(true);
    covered.setVisible(false);
    layer.getChildren().add(lockRoot);
    locked.set(true);
    view.focusPassword();
  }

  public boolean isLocked() {
    return locked.get();
  }

  public ReadOnlyBooleanProperty lockedProperty() {
    return locked.getReadOnlyProperty();
  }

  /**
   * Ends locking for good: removes the lock screen and the input listeners and stops the timer.
   * Called when the signed-in window goes away (sign-out, session ended).
   */
  public void stop() {
    if (stopped) {
      return;
    }
    stopped = true;
    idle.stop();
    removeLockScreen();
    attach(layer.getScene(), null);
    layer.sceneProperty().removeListener(sceneListener);
  }

  private void unlocked() {
    Node restore = focusBefore;
    removeLockScreen();
    if (restore != null && restore.getScene() != null && !restore.isDisabled()) {
      restore.requestFocus();
    }
    restartIdleTimer();
  }

  private void sessionEnded() {
    stop();
    onSessionEnded.run();
  }

  private void signedOut() {
    stop();
    onSignedOut.run();
  }

  private void removeLockScreen() {
    if (lockRoot != null) {
      layer.getChildren().remove(lockRoot);
      lockRoot = null;
    }
    covered.setVisible(true);
    covered.setDisable(false);
    Scene scene = layer.getScene();
    if (scene != null) {
      setAside.forEach(scene.getAccelerators()::putIfAbsent);
    }
    setAside.clear();
    focusBefore = null;
    locked.set(false);
  }

  private void restartIdleTimer() {
    if (timing && !stopped && !locked.get()) {
      idle.playFromStart();
    }
  }

  private void attach(Scene old, Scene now) {
    if (old != null) {
      old.removeEventFilter(InputEvent.ANY, activity);
    }
    if (now != null && !stopped) {
      now.addEventFilter(InputEvent.ANY, activity);
    }
  }
}
