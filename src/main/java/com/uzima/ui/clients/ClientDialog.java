package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.County;
import com.uzima.ui.Backend;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.FormField;
import com.uzima.ui.component.OverlayHost;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Register a client, or edit one. Every field from the spec: name, category, phone, optional email,
 * county (all 47, with type-ahead), address and the borehole's GPS location with a non-blocking
 * warning outside Kenya. In edit mode the category is read-only; users allowed to change it get a
 * "Change category…" button that opens {@link CategoryChangeDialog}.
 */
public final class ClientDialog {

  private final Backend backend;
  private final ClientFormViewModel model;
  private final DialogShell dialog;
  private final Map<String, FormField> fields = new LinkedHashMap<>();
  private final Consumer<Client> onChanged;
  private Label categoryValue;

  private ClientDialog(
      Backend backend,
      ClientFormViewModel model,
      Consumer<Client> onSaved,
      Consumer<Client> onChanged) {
    this.backend = backend;
    this.model = model;
    this.onChanged = onChanged;
    this.dialog =
        new DialogShell(
            model.isEdit() ? "Edit client" : "Register a client", DialogShell.Size.LARGE);
    model
        .existing()
        .ifPresentOrElse(
            c -> dialog.setSubtitle(c.code().value() + " · " + c.category().label()),
            () -> dialog.setSubtitle("The client ID is issued when you save."));
    dialog.setContent(form());
    dialog.setPrimary(model.isEdit() ? "Save changes" : "Register client", false);
    FormBinding.failure(dialog, model.failureProperty());
    model
        .busyProperty()
        .addListener(
            (obs, was, busy) -> {
              dialog.setBusy(busy);
              if (!busy) {
                focusFirstError();
              }
            });
    dialog.setOnPrimary(
        d ->
            model.save(
                saved -> {
                  d.close(DialogShell.Outcome.CONFIRMED);
                  onSaved.accept(saved);
                }));
  }

  /** The dialog for registering a new client. */
  public static DialogShell register(Backend backend, Consumer<Client> onSaved) {
    return new ClientDialog(backend, new ClientFormViewModel(backend), onSaved, c -> {}).dialog;
  }

  /**
   * The dialog for editing {@code client}. {@code onChanged} also runs when the category is changed
   * from inside the dialog, which saves straight away.
   */
  public static DialogShell edit(Backend backend, Client client, Consumer<Client> onChanged) {
    return new ClientDialog(backend, new ClientFormViewModel(backend, client), onChanged, onChanged)
        .dialog;
  }

  private Node form() {
    TextField name = text("name", model.nameProperty());
    name.setPromptText("e.g. Wanjiru Kamau or Baraka Hotels Ltd");
    TextField phone = text("phone", model.phoneProperty());
    phone.setPromptText("0712 345 678");
    phone.getStyleClass().add("figures");
    phone
        .focusedProperty()
        .addListener(
            (obs, was, focused) -> {
              if (!focused) {
                model.phoneEdited();
              }
            });
    TextField email = text("email", model.emailProperty());
    email.setPromptText("name@example.co.ke");

    ComboBox<County> county = new ComboBox<>();
    // Alphabetical, as people look for a county by name (the enum is in official code order).
    county
        .getItems()
        .setAll(
            java.util.Arrays.stream(County.values())
                .sorted(java.util.Comparator.comparing(County::label))
                .toList());
    county.setConverter(labels(County::label));
    county.valueProperty().bindBidirectional(model.countyProperty());
    county.setPromptText("Choose a county");
    county.setMaxWidth(Double.MAX_VALUE);
    county.setVisibleRowCount(12);
    county.setId("county");
    TypeAhead.install(county, County::label);

    TextArea address = new TextArea();
    address.textProperty().bindBidirectional(model.addressProperty());
    address.setPrefRowCount(2);
    address.setWrapText(true);
    address.setPromptText("Plot, road, area");
    address.setId("address");
    TabMovesFocus.install(address);

    TextField latitude = text("latitude", model.latitudeProperty());
    latitude.setPromptText("e.g. -1.2921");
    latitude.getStyleClass().add("figures");
    TextField longitude = text("longitude", model.longitudeProperty());
    longitude.setPromptText("e.g. 36.8219");
    longitude.getStyleClass().add("figures");

    FormField nameField = field("name", "Name", name, true);
    FormField categoryField = field("category", "Category", categoryInput(), !model.isEdit());
    FormField phoneField = field("phone", "Phone", phone, true);
    FormBinding.help(phoneField, model.phoneHelpProperty());
    FormField emailField = field("email", "Email", email, false);
    emailField.setHelp("Optional.");
    FormField countyField = field("county", "County", county, true);
    FormField addressField = field("address", "Address", address, false);
    FormField latField = field("latitude", "Latitude", latitude, false);
    FormField lonField = field("longitude", "Longitude", longitude, false);

    GridPane grid = new GridPane();
    grid.getStyleClass().add("client-form-grid");
    ColumnConstraints half = new ColumnConstraints();
    half.setPercentWidth(50);
    half.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(half, half);
    grid.add(nameField, 0, 0, 2, 1);
    grid.addRow(1, categoryField, phoneField);
    grid.addRow(2, emailField, countyField);
    grid.add(addressField, 0, 3, 2, 1);

    Label gpsTitle = new Label("Borehole location (GPS)");
    gpsTitle.getStyleClass().add("text-subtitle");
    Label gpsHelp =
        new Label(
            "Optional. Decimal degrees from a phone's map app; enter both numbers or neither.");
    gpsHelp.getStyleClass().add("text-caption");
    gpsHelp.setWrapText(true);
    GridPane gps = new GridPane();
    gps.getStyleClass().add("client-form-grid");
    gps.getColumnConstraints().addAll(half, half);
    gps.addRow(0, latField, lonField);
    Label warning = new Label();
    warning.getStyleClass().add("form-warning");
    warning.setWrapText(true);
    warning.setId("gpsWarning");
    warning.textProperty().bind(model.gpsWarningProperty());
    warning.visibleProperty().bind(model.gpsWarningProperty().isNotEmpty());
    warning.managedProperty().bind(warning.visibleProperty());

    VBox location = new VBox(new VBox(2, gpsTitle, gpsHelp), gps, warning);
    location.getStyleClass().addAll("client-form-section", "stack-12");
    VBox form = new VBox(grid, location);
    form.getStyleClass().add("stack-20");
    return form;
  }

  /** A category picker when registering; the fixed category and, if allowed, a change button. */
  private Node categoryInput() {
    if (!model.isEdit()) {
      ComboBox<ClientCategory> category = new ComboBox<>();
      category.getItems().setAll(ClientCategory.values());
      category.setConverter(CategoryChangeDialog.categoryLabels());
      category.valueProperty().bindBidirectional(model.categoryProperty());
      category.setPromptText("Choose a category");
      category.setMaxWidth(Double.MAX_VALUE);
      category.setId("category");
      TypeAhead.install(category, ClientCategory::label);
      return category;
    }
    categoryValue = new Label(model.categoryProperty().get().label());
    categoryValue.getStyleClass().add("client-category-value");
    categoryValue.setId("categoryValue");
    model
        .categoryProperty()
        .addListener((obs, old, now) -> categoryValue.setText(now == null ? "" : now.label()));
    HBox row = new HBox(categoryValue);
    row.getStyleClass().add("client-category-row");
    if (CategoryChangeViewModel.allowed(backend)) {
      Button change = new Button("Change category…");
      change.getStyleClass().addAll("small");
      change.setId("changeCategory");
      change.setOnAction(e -> openCategoryChange());
      row.getChildren().add(change);
    }
    return row;
  }

  private void openCategoryChange() {
    Client current = model.existing().orElseThrow();
    Client shown =
        current.category() == model.categoryProperty().get()
            ? current
            : current.withCategory(model.categoryProperty().get());
    DialogShell change =
        CategoryChangeDialog.build(
            new CategoryChangeViewModel(backend, shown),
            changed -> {
              model.categoryProperty().set(changed.category());
              dialog.setSubtitle(changed.code().value() + " · " + changed.category().label());
              OverlayHost.of(dialog)
                  .success(
                      "Category changed",
                      changed.code().value()
                          + " · "
                          + changed.name()
                          + " is now "
                          + changed.category().label());
              onChanged.accept(changed);
            });
    OverlayHost.of(dialog).showDialog(change);
  }

  private TextField text(String id, javafx.beans.property.StringProperty value) {
    TextField field = new TextField();
    field.textProperty().bindBidirectional(value);
    field.setId(id);
    return field;
  }

  private FormField field(String name, String label, Node input, boolean required) {
    FormField field = new FormField(label, input);
    field.setName(name);
    field.setRequired(required);
    FormBinding.error(field, model.errorProperty(name));
    fields.put(name, field);
    return field;
  }

  private void focusFirstError() {
    fields.values().stream()
        .filter(FormField::hasError)
        .findFirst()
        .ifPresent(f -> f.getInput().requestFocus());
  }

  private static <T> StringConverter<T> labels(java.util.function.Function<T, String> label) {
    return new StringConverter<>() {
      @Override
      public String toString(T value) {
        return value == null ? "" : label.apply(value);
      }

      @Override
      public T fromString(String s) {
        return null;
      }
    };
  }
}
