package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.FormField;
import java.util.function.Consumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** The small "Change category" dialog opened from the client form. */
final class CategoryChangeDialog {

  private CategoryChangeDialog() {}

  static DialogShell build(CategoryChangeViewModel model, Consumer<Client> onChanged) {
    Label current =
        new Label(
            model.client().name()
                + " is "
                + model.client().category().label()
                + " ("
                + model.client().code().value()
                + ").");
    current.setWrapText(true);

    ComboBox<ClientCategory> target = new ComboBox<>();
    target.getItems().setAll(model.choices());
    target.setConverter(categoryLabels());
    target.valueProperty().bindBidirectional(model.targetProperty());
    target.setMaxWidth(Double.MAX_VALUE);
    target.setId("newCategory");
    FormField field = new FormField("New category", target);
    field.setName("category");
    field.setRequired(true);
    FormBinding.error(field, model.errorProperty());

    Label note = new Label(model.auditNote());
    note.setWrapText(true);
    note.getStyleClass().addAll("client-audit-note");

    VBox body = new VBox(current, field, note);
    body.getStyleClass().add("stack-16");

    DialogShell dialog = new DialogShell("Change category", DialogShell.Size.SMALL);
    dialog.setSubtitle(model.client().code().value() + " · " + model.client().name());
    dialog.setContent(body);
    dialog.setPrimary("Change category", false);
    FormBinding.failure(dialog, model.failureProperty());
    model.busyProperty().addListener((obs, was, busy) -> dialog.setBusy(busy));
    dialog.setOnPrimary(
        d ->
            model.change(
                changed -> {
                  d.close(DialogShell.Outcome.CONFIRMED);
                  onChanged.accept(changed);
                }));
    return dialog;
  }

  static StringConverter<ClientCategory> categoryLabels() {
    return new StringConverter<>() {
      @Override
      public String toString(ClientCategory c) {
        return c == null ? "" : c.label();
      }

      @Override
      public ClientCategory fromString(String s) {
        return null;
      }
    };
  }
}
