package com.uzima.ui.shell;

import com.uzima.ui.component.OverlayHost;
import javafx.scene.Node;

/**
 * How a page moves around the shell: open another sidebar page, or show a detail page on top of the
 * current one. The top bar then shows the trail ("Clients › Wanjiru Kamau") with a Back button;
 * Alt+Left also goes back.
 */
public interface Navigator {

  /** Opens the sidebar page with this id ("clients", "jobs"); false if the user can't see it. */
  boolean open(String destinationId);

  /** Shows {@code page} on top of the current page, titled {@code title} in the top bar. */
  void push(String title, Node page);

  /** Swaps the current detail page for another, e.g. the saved job's detail after its editor. */
  void replace(String title, Node page);

  /** Goes back one page; does nothing on a sidebar page. */
  void back();

  /** The window's dialogs, toasts and loading overlay. */
  OverlayHost overlays();
}
