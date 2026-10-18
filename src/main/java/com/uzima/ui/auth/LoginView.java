package com.uzima.ui.auth;

import com.uzima.ui.Views;
import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** The sign-in screen. Binds {@link LoginViewModel} to login.fxml; holds no logic of its own. */
public final class LoginView {

  private final LoginViewModel model;
  private final Parent root;

  @FXML private Banner banner;
  @FXML private FormField usernameField;
  @FXML private FormField passwordField;
  @FXML private TextField username;
  @FXML private PasswordField password;
  @FXML private Button signIn;

  public LoginView(LoginViewModel model) {
    this.model = model;
    this.root = AuthLayout.wrap(Views.load("login", this));
  }

  public Parent root() {
    return root;
  }

  @FXML
  private void initialize() {
    username.textProperty().bindBidirectional(model.usernameProperty());
    password.textProperty().bindBidirectional(model.passwordProperty());
    Bindings.error(usernameField, model.usernameErrorProperty());
    Bindings.error(passwordField, model.passwordErrorProperty());
    Bindings.banner(banner, model.failureProperty());

    username.disableProperty().bind(model.busyProperty());
    password.disableProperty().bind(model.busyProperty());
    signIn.disableProperty().bind(model.busyProperty());
    signIn.textProperty().bind(model.busyProperty().map(busy -> busy ? "Signing in…" : "Sign in"));
    // After a failure, put the cursor back in the password box for the next attempt.
    model
        .failureProperty()
        .addListener(
            (obs, old, now) -> {
              if (!now.isEmpty()) {
                password.requestFocus();
              }
            });
    Platform.runLater(username::requestFocus);
  }

  @FXML
  private void signIn() {
    model.signIn();
    if (!model.usernameErrorProperty().get().isEmpty()) {
      username.requestFocus();
    } else if (!model.passwordErrorProperty().get().isEmpty()) {
      password.requestFocus();
    }
  }
}
