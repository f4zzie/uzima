package com.uzima.ui.clients;

import com.uzima.ui.component.Banner;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.FormField;
import javafx.beans.value.ObservableStringValue;

/** One-way bindings from view-model messages to form components. */
final class FormBinding {

  private FormBinding() {}

  static void error(FormField field, ObservableStringValue message) {
    field.setError(message.get());
    message.addListener((obs, old, now) -> field.setError(now));
  }

  static void help(FormField field, ObservableStringValue text) {
    field.setHelp(text.get());
    text.addListener((obs, old, now) -> field.setHelp(now));
  }

  /** A dialog's form-level banner, shown only for errors that belong to no field. */
  static void failure(DialogShell dialog, ObservableStringValue message) {
    message.addListener(
        (obs, old, now) -> {
          if (now == null || now.isEmpty()) {
            dialog.clearError();
          } else {
            dialog.showError(now);
          }
        });
  }

  static void banner(Banner banner, ObservableStringValue message) {
    apply(banner, message.get());
    message.addListener((obs, old, now) -> apply(banner, now));
  }

  private static void apply(Banner banner, String text) {
    if (text == null || text.isEmpty()) {
      banner.hide();
    } else {
      banner.showError(text);
    }
  }
}
