package com.uzima.ui.quote;

import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.LineCategory;
import com.uzima.service.ClientSummary;
import com.uzima.ui.Backend;
import com.uzima.ui.Pages;
import com.uzima.ui.component.Banner;
import com.uzima.ui.component.ConfirmDialog;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.FormField;
import com.uzima.ui.component.MetresField;
import com.uzima.ui.component.OverlayHost;
import com.uzima.ui.component.PageHeader;
import com.uzima.ui.component.StatePane;
import com.uzima.ui.component.TypeAhead;
import com.uzima.ui.shell.LeaveGuard;
import com.uzima.ui.shell.Navigator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * The quote editor: pick a client, switch on the services, and watch the price breakdown update as
 * you type. Opens for a new quote (optionally for a given client) or to edit a job that is still
 * pending survey, keeping the prices it was quoted at unless the user re-prices it.
 *
 * <p>Keyboard: Ctrl+S saves; Back or a sidebar click with unsaved changes asks first.
 */
public final class JobEditorPage {

  private static final KeyCombination SAVE =
      new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);

  private final Navigator nav;
  private final Pages pages;
  private final QuoteEditorViewModel model;
  private final Map<String, FormField> fields = new LinkedHashMap<>();
  private final Banner banner = new Banner();
  private final StatePane root;
  private final Button save = new Button("Save quote");

  public JobEditorPage(
      Backend backend, Navigator nav, Pages pages, OptionalLong clientId, OptionalLong jobId) {
    this(nav, pages, new QuoteEditorViewModel(backend, jobId, clientId, 150));
  }

  JobEditorPage(Navigator nav, Pages pages, QuoteEditorViewModel model) {
    this.nav = nav;
    this.pages = pages;
    this.model = model;
    HBox layout = new HBox(form(), new BreakdownCard(model));
    layout.getStyleClass().add("quote-editor");
    root = new StatePane(layout);
    root.getStyleClass().add("quote-editor-root");
    model.stateProperty().addListener((obs, old, now) -> render());
    model.errors().addListener((MapChangeListener<String, String>) c -> showErrors());
    model.failureProperty().addListener((obs, old, now) -> showFailure(now));
    LeaveGuard.attach(root, this::allowLeave);
    root.sceneProperty().addListener((obs, old, now) -> moveShortcut(old, now));
    model.load();
    render();
  }

  /** The node to put in the shell. */
  public Node root() {
    return root;
  }

  /** The top-bar title: "New quote", or the job number when editing. */
  public String title() {
    return model.isEdit() ? "Edit quote" : "New quote";
  }

  QuoteEditorViewModel model() {
    return model;
  }

  // ------------------------------------------------------------------ layout

  private Node form() {
    PageHeader header =
        new PageHeader(
            model.isEdit()
                ? "Change what this job includes. Saved prices stay as quoted."
                : "Prices come from today's catalog and the client's category.");
    Button cancel = new Button("Cancel");
    cancel.setOnAction(e -> nav.back());
    save.getStyleClass().add("primary");
    save.setId("saveQuote");
    save.setAccessibleHelp("Ctrl+S");
    save.disableProperty().bind(model.busyProperty());
    save.textProperty().bind(model.busyProperty().map(b -> b ? "Saving…" : "Save quote"));
    save.setOnAction(e -> save());
    header.getActions().addAll(cancel, save);

    VBox sections = new VBox();
    sections.getStyleClass().add("quote-form");
    sections
        .getChildren()
        .addAll(
            header,
            banner,
            repriceNotice(),
            clientCard(),
            new ServiceSection(
                "Drilling",
                "Down payment plus a rate per metre by depth band",
                model.drillingOnProperty(),
                LineCategory.DRILLING,
                model.quoteProperty(),
                drilling()),
            new ServiceSection(
                "Pump",
                "Pump price plus a rate per metre of lift height",
                model.pumpOnProperty(),
                LineCategory.PUMP,
                model.quoteProperty(),
                pump()),
            new ServiceSection(
                "Tank",
                "One storage tank at catalog price",
                model.tankOnProperty(),
                LineCategory.TANK,
                model.quoteProperty(),
                tank()),
            new ServiceSection(
                "Plumbing",
                "Pipe runs and outlets, or one agreed amount",
                model.plumbingOnProperty(),
                LineCategory.PLUMBING,
                model.quoteProperty(),
                new PlumbingEditor(model, this::register)),
            notes());
    ScrollPane scroll = new ScrollPane(sections);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("quote-form-scroll");
    HBox.setHgrow(scroll, Priority.ALWAYS);
    return scroll;
  }

  private Node clientCard() {
    ComboBox<ClientSummary> client = new ComboBox<>(model.clients());
    client.setId("client");
    client.setConverter(labels(c -> c.code().value() + " · " + c.name()));
    client.setCellFactory(
        list ->
            new javafx.scene.control.ListCell<>() {
              @Override
              protected void updateItem(ClientSummary c, boolean empty) {
                super.updateItem(c, empty);
                setText(
                    empty || c == null
                        ? null
                        : c.code().value() + " · " + c.name() + "  (" + c.category().label() + ")");
              }
            });
    client.valueProperty().bindBidirectional(model.clientProperty());
    client.setPromptText("Choose a client");
    client.setMaxWidth(Double.MAX_VALUE);
    client.setDisable(model.isEdit());
    TypeAhead.install(client, ClientSummary::name);
    FormField field = new FormField("Client", client);
    field.setRequired(true);
    field.setHelp(
        model.isEdit()
            ? "A saved quote stays with its client."
            : "The client's category sets the survey and authority fees.");
    register("client", field);
    VBox card = new VBox(field);
    card.getStyleClass().add("quote-client");
    return card;
  }

  private Node drilling() {
    ComboBox<DrillingType> type = new ComboBox<>(model.drillingTypes());
    type.setId("drillingType");
    type.setConverter(
        labels(d -> d.name() + " · KSh " + d.downPayment().formatCompact() + " down"));
    type.valueProperty().bindBidirectional(model.drillingTypeProperty());
    type.setPromptText("Choose a type");
    TypeAhead.install(type, DrillingType::name);
    MetresField depth = new MetresField();
    depth.setId("depth");
    depth.valueProperty().bindBidirectional(model.depthProperty());
    depth.setPromptText("0.00");
    return grid(field("drillingType", "Drilling type", type), field("depth", "Depth", depth));
  }

  private Node pump() {
    ComboBox<PumpType> type = new ComboBox<>(model.pumpTypes());
    type.setId("pumpType");
    type.setConverter(labels(p -> p.name() + " · KSh " + p.baseCost().formatCompact()));
    type.valueProperty().bindBidirectional(model.pumpTypeProperty());
    type.setPromptText("Choose a pump");
    TypeAhead.install(type, PumpType::name);
    MetresField lift = new MetresField();
    lift.setId("lift");
    lift.valueProperty().bindBidirectional(model.liftProperty());
    lift.setPromptText("0.00");
    FormField liftField = field("lift", "Lift height", lift);
    liftField.setHelp("0 when the pump sits at ground level");
    return grid(field("pumpType", "Pump type", type), liftField);
  }

  private Node tank() {
    ComboBox<TankType> capacity = new ComboBox<>(model.tanks());
    capacity.setId("tank");
    capacity.setConverter(labels(t -> t.displayName() + " · KSh " + t.price().formatCompact()));
    capacity.valueProperty().bindBidirectional(model.tankProperty());
    capacity.setPromptText("Choose a size");
    TypeAhead.install(capacity, TankType::displayName);
    return grid(field("tank", "Capacity", capacity), new VBox());
  }

  private Node notes() {
    TextArea text = new TextArea();
    text.textProperty().bindBidirectional(model.notesProperty());
    text.setPrefRowCount(3);
    text.setWrapText(true);
    text.setPromptText("Site access, contacts, anything the crew should know");
    FormField field = new FormField("Notes", text);
    field.getStyleClass().add("quote-notes");
    return field;
  }

  private Node repriceNotice() {
    Label text = new Label();
    text.setWrapText(true);
    text.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(text, Priority.ALWAYS);
    text.textProperty()
        .bind(
            model
                .jobProperty()
                .map(
                    j ->
                        "Prices are as quoted on "
                            + DisplayFormats.DATE.format(j.createdAt().atZone(DisplayFormats.ZONE))
                            + ". Re-price to use today's catalog, fees and VAT.")
                .orElse(""));
    Button reprice = new Button("Re-price…");
    reprice.getStyleClass().add("small");
    reprice.setId("reprice");
    reprice.setOnAction(e -> RepriceDialog.open(model, nav.overlays()));
    reprice.disableProperty().bind(model.dirtyProperty());
    reprice.setAccessibleHelp("Save or undo your changes first");
    HBox notice = new HBox(12, text, reprice);
    notice.setAlignment(Pos.CENTER_LEFT);
    notice.getStyleClass().add("quote-notice");
    notice.setVisible(model.isEdit());
    notice.setManaged(model.isEdit());
    return notice;
  }

  private FormField field(String key, String label, Node input) {
    FormField field = new FormField(label, input);
    register(key, field);
    if (input instanceof ComboBox<?> box) {
      box.setMaxWidth(Double.MAX_VALUE);
    }
    return field;
  }

  private static GridPane grid(Node left, Node right) {
    GridPane grid = new GridPane();
    grid.getStyleClass().add("quote-grid");
    grid.addRow(0, left, right);
    var half = new javafx.scene.layout.ColumnConstraints();
    half.setPercentWidth(50);
    var other = new javafx.scene.layout.ColumnConstraints();
    other.setPercentWidth(50);
    grid.getColumnConstraints().addAll(half, other);
    return grid;
  }

  private void register(String key, FormField field) {
    field.setName(key);
    fields.put(key, field);
  }

  static <T> StringConverter<T> labels(Function<T, String> label) {
    return new StringConverter<>() {
      @Override
      public String toString(T item) {
        return item == null ? "" : label.apply(item);
      }

      @Override
      public T fromString(String text) {
        return null;
      }
    };
  }

  // ------------------------------------------------------------------ behaviour

  private void render() {
    switch (model.stateProperty().get()) {
      case LOADING -> root.showLoading("Loading clients and prices…");
      case ERROR -> root.showError(QuoteEditorViewModel.LOAD_FAILED, model::load);
      case READY -> {
        root.showContent();
        if (model.clientProperty().get() == null) {
          // Content is visible and managed in the scene now; focus synchronously so keyboard
          // users (and tests) land on the client field without waiting for a pulse.
          focus("client");
        }
      }
      case null -> root.showLoading("Loading…");
    }
  }

  private void save() {
    model.save(this::saved);
    Platform.runLater(this::focusFirstError);
  }

  private void saved(Job job) {
    OverlayHost overlays = nav.overlays();
    if (model.isEdit()) {
      overlays.success(
          "Quote updated",
          job.number().value() + " · KSh " + job.quote().grandTotal().formatPlain());
      nav.back();
    } else {
      overlays.success(
          "Quote saved", job.number().value() + " · KSh " + job.quote().grandTotal().formatPlain());
      nav.back();
      pages.job(job.id());
    }
  }

  private void showErrors() {
    fields.forEach((key, field) -> field.setError(model.errors().get(key)));
  }

  private void focusFirstError() {
    fields.entrySet().stream()
        .filter(e -> e.getValue().hasError() && e.getValue().getScene() != null)
        .findFirst()
        .ifPresent(e -> focus(e.getKey()));
  }

  private void focus(String key) {
    focus(key, 0);
  }

  private void focus(String key, int attempt) {
    FormField field = fields.get(key);
    if (field == null || field.getInput() == null) {
      return;
    }
    Node input = field.getInput();
    input.requestFocus();
    if (!input.isFocused() && attempt < 5) {
      // The pane may not yet be laid out in the scene; retry on the next pulse so the focus
      // request lands once the node is showing. Capped so a mis-wired field can't loop forever.
      Platform.runLater(() -> focus(key, attempt + 1));
    }
  }

  private void showFailure(String message) {
    if (message == null || message.isEmpty()) {
      banner.hide();
    } else {
      banner.showError(message);
    }
  }

  private boolean allowLeave(Runnable leave) {
    if (!model.dirtyProperty().get()) {
      return true;
    }
    DialogShell dialog =
        new ConfirmDialog(
                "Discard changes?",
                "Your changes to this quote haven't been saved. Leave without saving?")
            .confirmLabel("Discard changes", true)
            .cancelLabel("Keep editing")
            .onConfirm(
                (shell, reason) -> {
                  model.dirtyProperty().set(false);
                  shell.close(DialogShell.Outcome.CONFIRMED);
                  leave.run();
                })
            .shell();
    nav.overlays().showDialog(dialog);
    return false;
  }

  private final Runnable saveShortcut = this::saveFromShortcut;

  private void saveFromShortcut() {
    if (!nav.overlays().isDialogOpen() && !model.busyProperty().get()) {
      save();
    }
  }

  private void moveShortcut(Scene old, Scene now) {
    if (old != null) {
      old.getAccelerators().remove(SAVE, saveShortcut);
    }
    if (now != null) {
      now.getAccelerators().put(SAVE, saveShortcut);
    }
  }
}
