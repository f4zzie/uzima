package com.uzima.ui.component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * The window's root: app content with layers above it for dialogs, a window-wide loading overlay
 * and toasts. Dialogs are in-window overlays rather than separate stages, so they share the
 * window's theme, show up in scene snapshots, and can be driven by TestFX.
 *
 * <p>While a dialog is open nothing underneath it can be clicked or typed into. Screens find their
 * host with {@link #of(Node)}.
 */
public class OverlayHost extends StackPane {

  /** At most this many toasts are shown; a new one pushes out the oldest. */
  public static final int MAX_TOASTS = 3;

  public static final Duration DEFAULT_TOAST_DURATION = Duration.seconds(4);

  private final Node content;
  private final InputBlocker contentBlocker;
  private final StackPane dialogLayer = new StackPane();
  private final VBox toastLayer = new VBox();
  private final LoadingOverlay loading = new LoadingOverlay();
  private final Deque<OpenDialog> dialogs = new ArrayDeque<>();
  private final ReadOnlyBooleanWrapper dialogOpen = new ReadOnlyBooleanWrapper(this, "dialogOpen");
  private Duration toastDuration = DEFAULT_TOAST_DURATION;

  public OverlayHost(Node content) {
    this.content = Objects.requireNonNull(content);
    getStyleClass().add("overlay-host");
    contentBlocker = new InputBlocker(content);
    loading.cover(content);

    dialogLayer.getStyleClass().add("dialog-layer");
    dialogLayer.setVisible(false);
    dialogLayer.addEventHandler(MouseEvent.ANY, MouseEvent::consume);

    toastLayer.getStyleClass().add("toast-layer");
    toastLayer.setAlignment(Pos.BOTTOM_RIGHT);
    toastLayer.setPickOnBounds(false);
    toastLayer.setFillWidth(false);

    getChildren().addAll(content, loading, dialogLayer, toastLayer);
  }

  /** The nearest host above {@code node}; throws if the node isn't inside one. */
  public static OverlayHost of(Node node) {
    for (Parent p = node instanceof Parent self ? self : node.getParent();
        p != null;
        p = p.getParent()) {
      if (p instanceof OverlayHost host) {
        return host;
      }
    }
    throw new IllegalStateException("No OverlayHost above " + node);
  }

  public Node getContent() {
    return content;
  }

  /**
   * Shows {@code dialog} above everything else and returns its outcome. Dialogs can stack (a
   * confirmation over an editor); only the top one takes input. Focus returns to where it was when
   * the dialog closes.
   */
  public CompletionStage<DialogShell.Outcome> showDialog(DialogShell dialog) {
    Node focusBefore = getScene() == null ? null : getScene().getFocusOwner();
    StackPane scrim = new StackPane(dialog);
    scrim.getStyleClass().add("dialog-scrim");
    StackPane.setAlignment(dialog, Pos.CENTER);
    dialog.setMaxHeight(USE_PREF_SIZE);

    OpenDialog previous = dialogs.peek();
    if (previous == null) {
      contentBlocker.block();
    } else {
      previous.scrim.setDisable(true);
    }
    OpenDialog open = new OpenDialog(dialog, scrim, focusBefore);
    dialogs.push(open);
    dialogLayer.getChildren().add(scrim);
    dialogLayer.setVisible(true);
    dialogOpen.set(true);
    dialog.result().thenRun(() -> closed(open));
    dialog.applyCss();
    dialog.focusInitial();
    return dialog.result();
  }

  public boolean isDialogOpen() {
    return dialogOpen.get();
  }

  public ReadOnlyBooleanProperty dialogOpenProperty() {
    return dialogOpen.getReadOnlyProperty();
  }

  /** The dialog on top, if any. */
  public DialogShell topDialog() {
    OpenDialog top = dialogs.peek();
    return top == null ? null : top.dialog;
  }

  /** Shows a toast in the bottom-right corner. */
  public Toast toast(Toast.Kind kind, String title, String message) {
    Toast toast = new Toast(kind, title, message);
    toast.setOnDismiss(() -> toastLayer.getChildren().remove(toast));
    while (toastLayer.getChildren().size() >= MAX_TOASTS) {
      toastLayer.getChildren().remove(0);
    }
    toastLayer.getChildren().add(toast);
    if (kind.autoHides()) {
      PauseTransition timer = new PauseTransition(toastDuration);
      timer.setOnFinished(e -> toastLayer.getChildren().remove(toast));
      timer.play();
    }
    return toast;
  }

  public Toast success(String title, String message) {
    return toast(Toast.Kind.SUCCESS, title, message);
  }

  public Toast info(String title, String message) {
    return toast(Toast.Kind.INFO, title, message);
  }

  public Toast error(String title, String message) {
    return toast(Toast.Kind.ERROR, title, message);
  }

  /** Toasts currently on screen, oldest first. */
  public java.util.List<Toast> toasts() {
    return toastLayer.getChildren().stream()
        .filter(Toast.class::isInstance)
        .map(Toast.class::cast)
        .toList();
  }

  /** How long success and info toasts stay; tests shorten it. */
  public void setToastDuration(Duration duration) {
    toastDuration = duration;
  }

  /** The window-wide loading overlay (covers the content, below dialogs). */
  public LoadingOverlay loading() {
    return loading;
  }

  private void closed(OpenDialog open) {
    dialogs.remove(open);
    dialogLayer.getChildren().remove(open.scrim);
    OpenDialog next = dialogs.peek();
    if (next == null) {
      dialogLayer.setVisible(false);
      dialogOpen.set(false);
      contentBlocker.release();
    } else {
      next.scrim.setDisable(false);
    }
    if (open.focusBefore != null && open.focusBefore.getScene() != null) {
      open.focusBefore.requestFocus();
    } else if (next != null) {
      next.dialog.focusInitial();
    }
  }

  private record OpenDialog(DialogShell dialog, StackPane scrim, Node focusBefore) {}
}
