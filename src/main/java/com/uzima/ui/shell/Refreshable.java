package com.uzima.ui.shell;

/**
 * A page that reloads its data when the user comes back to it, e.g. a list after a record on a
 * detail page was edited.
 */
public interface Refreshable {

  void refresh();

  /**
   * Marks {@code page} (the node a page puts in the shell) as refreshed by {@code refreshable}, for
   * page classes that hold their node rather than being it.
   */
  static <N extends javafx.scene.Node> N attach(N page, Refreshable refreshable) {
    page.getProperties().put(Refreshable.class, refreshable);
    return page;
  }

  /** The refresher for {@code page}: the node itself, or one attached with {@link #attach}. */
  static java.util.Optional<Refreshable> of(javafx.scene.Node page) {
    if (page instanceof Refreshable self) {
      return java.util.Optional.of(self);
    }
    return page.getProperties().get(Refreshable.class) instanceof Refreshable r
        ? java.util.Optional.of(r)
        : java.util.Optional.empty();
  }
}
