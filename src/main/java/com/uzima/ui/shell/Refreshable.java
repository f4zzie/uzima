package com.uzima.ui.shell;

/**
 * A page that reloads its data when the user comes back to it, e.g. a list after a record on a
 * detail page was edited.
 */
public interface Refreshable {

  void refresh();
}
