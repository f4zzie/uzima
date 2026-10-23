package com.uzima.ui.auth;

import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import javafx.scene.AccessibleRole;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The lock screen: covers the whole window until the signed-in user enters their password again.
 * Binds {@link LockViewModel}; {@link ScreenLock} decides when it shows.
 */
public final class LockView {

  private final PasswordField password = new PasswordField();
  private final Parent root;

  public LockView(LockViewModel model) {
    String name = model.displayName();
    Label overline = new Label("UZIMA IS LOCKED");
    overline.getStyleClass().add("text-overline");
    Label headline = new Label("Locked — " + name);
    headline.setId("lockTitle");
    headline.getStyleClass().add("text-headline");
    headline.setWrapText(true);
    Label intro = new Label("Enter your password to carry on where you left off.");
    intro.getStyleClass().add("text-muted");
    intro.setWrapText(true);
    VBox heading = new VBox(overline, headline, intro);
    heading.getStyleClass().add("auth-heading");

    Banner banner = new Banner();
    Bindings.banner(banner, model.failureProperty());

    password.setId("unlockPassword");
    password.textProperty().bindBidirectional(model.passwordProperty());
    password.disableProperty().bind(model.busyProperty());
    FormField passwordField = new FormField("Password", password);
    passwordField.setName("password");
    Bindings.error(passwordField, model.passwordErrorProperty());

    Button unlock = new Button();
    unlock.setId("unlock");
    unlock.setDefaultButton(true);
    unlock.getStyleClass().addAll("primary", "large", "auth-submit");
    unlock.setMaxWidth(Double.MAX_VALUE);
    unlock.disableProperty().bind(model.busyProperty());
    unlock.textProperty().bind(model.busyProperty().map(busy -> busy ? "Checking…" : "Unlock"));
    unlock.setOnAction(
        e -> {
          model.unlock();
          if (passwordField.hasError()) {
            password.requestFocus();
          }
        });
    // A wrong password clears the box; put the cursor back for the next try.
    model
        .busyProperty()
        .addListener(
            (obs, was, busy) -> {
              if (!busy && password.getScene() != null) {
                password.requestFocus();
              }
            });

    Label someoneElse = new Label("Not " + firstName(name) + "?");
    someoneElse.getStyleClass().add("text-caption");
    Hyperlink signOut = new Hyperlink("Sign out");
    signOut.setId("lockSignOut");
    signOut.disableProperty().bind(model.busyProperty());
    signOut.setOnAction(e -> model.signOut());
    HBox alternative = new HBox(someoneElse, signOut);
    alternative.getStyleClass().add("auth-alternative");

    VBox card = new VBox(heading, banner, passwordField, unlock, alternative);
    card.getStyleClass().add("auth-card");
    card.setMaxHeight(VBox.USE_PREF_SIZE);
    root = AuthLayout.wrap(card);
    root.getStyleClass().add("lock-screen");
    root.setAccessibleRole(AccessibleRole.DIALOG);
    root.setAccessibleText("Uzima is locked");
  }

  public Parent root() {
    return root;
  }

  public void focusPassword() {
    password.requestFocus();
  }

  private static String firstName(String displayName) {
    String trimmed = displayName.trim();
    return trimmed.isEmpty() ? "you" : trimmed.split("\\s+")[0];
  }
}
