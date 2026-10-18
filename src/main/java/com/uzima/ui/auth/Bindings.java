package com.uzima.ui.auth;

import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import javafx.beans.value.ObservableStringValue;

/** Small one-way bindings from view-model messages to components. */
final class Bindings {

  private Bindings() {}

  static void error(FormField field, ObservableStringValue message) {
    field.setError(message.get());
    message.addListener((obs, old, now) -> field.setError(now));
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
