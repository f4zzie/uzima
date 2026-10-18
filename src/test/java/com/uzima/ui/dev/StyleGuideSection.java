package com.uzima.ui.dev;

import javafx.scene.Node;

/** One section of the dev-only style guide. */
public interface StyleGuideSection {

  String title();

  /** A short line under the title saying what the section shows and when to use it. */
  String description();

  /** Builds the section's specimens. Called on the FX thread. */
  Node build();
}
