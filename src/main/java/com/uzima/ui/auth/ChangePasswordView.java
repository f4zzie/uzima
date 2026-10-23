package com.uzima.ui.auth;

import com.uzima.ui.component.Banner;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The forced change-password screen, shown after signing in with a temporary password. The shell
 * stays out of reach until the password is changed; the only other way out is signing out.
 */
public final class ChangePasswordView {

  static final String CURRENT_HELP = "The temporary password you just signed in with.";

  private final ChangePasswordViewModel model;
  private final ChangePasswordForm form;
  private final Parent root;

  /**
   * @param firstName greets the user, so they can tell it is their own account
   * @param onSignedOut runs after "Sign out" has ended the session
   */
  public ChangePasswordView(ChangePasswordViewModel model, String firstName, Runnable onSignedOut) {
    this.model = model;
    this.form = new ChangePasswordForm(model, CURRENT_HELP);

    Label overline = new Label("FIRST SIGN-IN");
    overline.getStyleClass().add("text-overline");
    Label headline = new Label("Choose your own password");
    headline.getStyleClass().add("text-headline");
    headline.setWrapText(true);
    Label intro =
        new Label(
            "Welcome, "
                + firstName
                + ". You signed in with a temporary password from an administrator. Choose a"
                + " password only you know to continue.");
    intro.getStyleClass().add("text-muted");
    intro.setWrapText(true);
    VBox heading = new VBox(overline, headline, intro);
    heading.getStyleClass().add("auth-heading");

    Banner banner = new Banner();
    Bindings.banner(banner, model.failureProperty());

    form.root().getStyleClass().add("auth-fields");

    Button submit = new Button();
    submit.setId("changePassword");
    submit.setDefaultButton(true);
    submit.getStyleClass().addAll("primary", "large", "auth-submit");
    submit.setMaxWidth(Double.MAX_VALUE);
    submit.disableProperty().bind(model.busyProperty());
    submit
        .textProperty()
        .bind(
            model
                .busyProperty()
                .map(busy -> busy ? "Changing password…" : "Change password and continue"));
    submit.setOnAction(e -> submit());

    Label notNow = new Label("Not your account?");
    notNow.getStyleClass().add("text-caption");
    Hyperlink signOut = new Hyperlink("Sign out");
    signOut.setId("signOut");
    signOut.disableProperty().bind(model.busyProperty());
    signOut.setOnAction(e -> model.signOut(onSignedOut));
    HBox alternative = new HBox(notNow, signOut);
    alternative.getStyleClass().add("auth-alternative");

    VBox card = new VBox(heading, banner, form.root(), submit, alternative);
    card.getStyleClass().add("auth-card");
    card.setMaxHeight(VBox.USE_PREF_SIZE);
    root = AuthLayout.wrap(card);

    // When the service answers with field errors, move the cursor to the first one.
    model
        .busyProperty()
        .addListener(
            (obs, was, busy) -> {
              if (!busy) {
                form.focusFirstError();
              }
            });
    Platform.runLater(form::focusCurrent);
  }

  public Parent root() {
    return root;
  }

  private void submit() {
    model.change();
    form.focusFirstError();
  }
}
