package com.uzima.ui.auth;

import com.uzima.ui.Views;
import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

/** First-run screen that creates the administrator. Binds {@link FirstRunViewModel}. */
public final class FirstRunView {

  private final FirstRunViewModel model;
  private final Parent root;

  @FXML private Banner banner;
  @FXML private FormField displayNameField;
  @FXML private FormField usernameField;
  @FXML private FormField passwordField;
  @FXML private FormField confirmField;
  @FXML private TextField displayName;
  @FXML private TextField username;
  @FXML private PasswordField password;
  @FXML private PasswordField confirm;
  @FXML private Button create;

  public FirstRunView(FirstRunViewModel model) {
    this.model = model;
    this.root = AuthLayout.wrap(Views.load("first-run", this));
  }

  public Parent root() {
    return root;
  }

  @FXML
  private void initialize() {
    passwordField.setHelp(FirstRunViewModel.PASSWORD_HINT);
    displayName.textProperty().bindBidirectional(model.displayNameProperty());
    username.textProperty().bindBidirectional(model.usernameProperty());
    password.textProperty().bindBidirectional(model.passwordProperty());
    confirm.textProperty().bindBidirectional(model.confirmProperty());
    Bindings.error(displayNameField, model.displayNameErrorProperty());
    Bindings.error(usernameField, model.usernameErrorProperty());
    Bindings.error(passwordField, model.passwordErrorProperty());
    Bindings.error(confirmField, model.confirmErrorProperty());
    Bindings.banner(banner, model.failureProperty());

    for (TextInputControl input : inputs()) {
      input.disableProperty().bind(model.busyProperty());
    }
    create.disableProperty().bind(model.busyProperty());
    create
        .textProperty()
        .bind(
            model
                .busyProperty()
                .map(busy -> busy ? "Creating account…" : "Create account and sign in"));
    // When the service answers with field errors, move the cursor to the first one.
    model
        .busyProperty()
        .addListener(
            (obs, was, now) -> {
              if (!now) {
                focusFirstError();
              }
            });
    Platform.runLater(displayName::requestFocus);
  }

  @FXML
  private void create() {
    model.create();
    focusFirstError();
  }

  private void focusFirstError() {
    List<FormField> fields = List.of(displayNameField, usernameField, passwordField, confirmField);
    fields.stream()
        .filter(FormField::hasError)
        .findFirst()
        .ifPresent(f -> f.getInput().requestFocus());
  }

  private List<TextInputControl> inputs() {
    return List.of(displayName, username, password, confirm);
  }
}
