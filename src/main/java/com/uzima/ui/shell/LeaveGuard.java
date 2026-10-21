package com.uzima.ui.shell;

import java.util.Optional;
import javafx.scene.Node;

/**
 * Lets a page stop the user from leaving it by accident, e.g. an editor with unsaved changes. The
 * shell asks before Back, Alt+Left or a sidebar click takes the user away.
 */
@FunctionalInterface
public interface LeaveGuard {

  /**
   * Returns true to let the user leave now. Otherwise the page asks (e.g. "Discard changes?") and
   * calls {@code leave} itself if the user agrees.
   */
  boolean allowLeave(Runnable leave);

  /** Installs {@code guard} on the node a page puts in the shell. */
  static <N extends Node> N attach(N page, LeaveGuard guard) {
    page.getProperties().put(LeaveGuard.class, guard);
    return page;
  }

  static Optional<LeaveGuard> of(Node page) {
    return page.getProperties().get(LeaveGuard.class) instanceof LeaveGuard g
        ? Optional.of(g)
        : Optional.empty();
  }
}
