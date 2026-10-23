package com.uzima.ui.auth;

import com.uzima.ui.component.FormBinding;
import com.uzima.ui.component.FormField;
import com.uzima.ui.component.PasswordStrengthHint;
import java.util.List;
import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

/**
 * The three password fields shared by the forced change-password screen and the voluntary dialog:
 * current password, new password with a strength hint, and confirmation.
 */
final class ChangePasswordForm {

  private final FormField currentField;
  private final FormField passwordField;
  private final FormField confirmField;
  private final PasswordField current;
  private final VBox root;

  ChangePasswordForm(ChangePasswordViewModel model, String currentHelp) {
    current = input("currentPassword", model.currentProperty());
    PasswordField password = input("newPassword", model.passwordProperty());
    PasswordField confirm = input("confirmPassword", model.confirmProperty());

    currentField = field("Current password", "currentPassword", current, currentHelp);
    passwordField = field("New password", "password", password, FirstRunViewModel.PASSWORD_HINT);
    passwordField.setAccessory(new PasswordStrengthHint(password.textProperty()));
    confirmField = field("Confirm new password", "confirm", confirm, null);

    FormBinding.error(currentField, model.currentErrorProperty());
    FormBinding.error(passwordField, model.passwordErrorProperty());
    FormBinding.error(confirmField, model.confirmErrorProperty());
    for (PasswordField input : List.of(current, password, confirm)) {
      input.disableProperty().bind(model.busyProperty());
    }

    root = new VBox(currentField, passwordField, confirmField);
  }

  /** The fields in a column; the caller adds the spacing class that matches its container. */
  VBox root() {
    return root;
  }

  /** Puts the cursor in the first field with an error; returns false when there is none. */
  boolean focusFirstError() {
    for (FormField field : List.of(currentField, passwordField, confirmField)) {
      if (field.hasError()) {
        field.getInput().requestFocus();
        return true;
      }
    }
    return false;
  }

  void focusCurrent() {
    current.requestFocus();
  }

  private static PasswordField input(String id, StringProperty value) {
    PasswordField input = new PasswordField();
    input.setId(id);
    input.textProperty().bindBidirectional(value);
    return input;
  }

  private static FormField field(String label, String name, PasswordField input, String help) {
    FormField field = new FormField(label, input);
    field.setName(name);
    field.setRequired(true);
    field.setHelp(help);
    return field;
  }
}
