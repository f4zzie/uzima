package com.uzima.ui.shell;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Shown in the content area when the signed-in user has no pages to open. */
final class WelcomePanel {

  private WelcomePanel() {}

  static Node build(ShellViewModel model) {
    Label heading =
        new Label("Signed in as " + model.displayName() + " (" + model.roleLabel() + ")");
    heading.getStyleClass().add("text-title");
    heading.setId("welcome-heading");
    Label hint = new Label("Use the account menu at the top right to sign out.");
    hint.getStyleClass().add("text-muted");
    VBox card = new VBox(heading, hint);
    card.getStyleClass().addAll("card", "stack-8");
    card.setMaxHeight(VBox.USE_PREF_SIZE);
    VBox page = new VBox(card);
    page.getStyleClass().add("page");
    return InitialFocus.attach(page, () -> heading);
  }
}
