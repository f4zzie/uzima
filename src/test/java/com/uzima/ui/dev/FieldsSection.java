package com.uzima.ui.dev;

import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Text fields with labels, help text and inline errors; form-level banners. */
final class FieldsSection implements StyleGuideSection {

  @Override
  public String title() {
    return "Fields and messages";
  }

  @Override
  public String description() {
    return "Errors appear under the field that caused them. Banners are only for problems that"
        + " belong to the whole form.";
  }

  @Override
  public Node build() {
    FormField name = new FormField("Client name", new TextField("Wanjiru Kamau"));
    name.setRequired(true);

    FormField phone = new FormField("Phone", new TextField());
    ((TextField) phone.getInput()).setPromptText("0712 345 678");
    phone.setHelp("Kenyan mobile or landline. Saved as +254...");

    FormField email = new FormField("Email", new TextField("wanjiru@"));
    email.setError("Enter a full email address, like name@example.com");

    FormField password = new FormField("Password", new PasswordField());
    password.setRequired(true);
    password.setError("Password is required");

    TextField disabledInput = new TextField("UZM-DOM-0007");
    disabledInput.setDisable(true);
    FormField disabled = new FormField("Client ID", disabledInput);
    disabled.setHelp("Assigned automatically and never changes.");

    GridPane grid = new GridPane();
    grid.getStyleClass().add("style-guide-grid");
    grid.addRow(0, width(name), width(phone));
    grid.addRow(1, width(email), width(password));
    grid.addRow(2, width(disabled));

    Banner error = new Banner();
    error.showError("This job changed since you opened it. Reload to see the latest version.");
    Banner info = new Banner();
    info.show(
        Banner.Kind.INFO, "Prices are saved with the quote. Re-price to use today's catalog.");
    Banner success = new Banner();
    success.show(Banner.Kind.SUCCESS, "Payment RCP-2026-00012 recorded.");
    Banner warning = new Banner();
    warning.show(Banner.Kind.WARNING, "Refund due: KSh 84,400.00");
    return new VBox(16, grid, error, info, success, warning);
  }

  private static FormField width(FormField field) {
    field.setPrefWidth(320);
    return field;
  }
}
