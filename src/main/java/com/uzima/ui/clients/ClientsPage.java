package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.service.ClientSummary;
import com.uzima.ui.Backend;
import com.uzima.ui.Pages;
import com.uzima.ui.component.ColumnWidth;
import com.uzima.ui.component.DataTable;
import com.uzima.ui.component.PageHeader;
import com.uzima.ui.component.StatusBadge;
import com.uzima.ui.shell.InitialFocus;
import com.uzima.ui.shell.Navigator;
import com.uzima.ui.shell.Refreshable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The Clients page: a searchable table of clients (active ones by default) with "New client"
 * (Ctrl+N). Enter or a double-click opens the client's detail page.
 */
public final class ClientsPage implements Refreshable, InitialFocus {

  private static final KeyCombination NEW =
      new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);

  private final Backend backend;
  private final Navigator nav;
  private final ClientsViewModel model;
  private final DataTable<ClientSummary> table = new DataTable<>();
  private final VBox root;
  private final Button add = new Button("New client");

  public ClientsPage(Backend backend, Navigator nav, Pages pages) {
    this.backend = backend;
    this.nav = nav;
    this.model = new ClientsViewModel(backend);

    table.setItemNouns("client", "clients");
    table.setSearchPrompt("Search by ID, name, phone or county");
    table.setSearchMatcher(
        DataTable.containsIn(
            c -> c.code().value(),
            ClientSummary::name,
            c -> c.phone().formatLocal() + " " + c.phone().value(),
            c -> c.county().label()));
    table.addFiguresColumn("Client ID", c -> c.code().value(), ColumnWidth.fixed(160));
    table.addTextColumn("Name", ClientSummary::name, ColumnWidth.flex(220));
    table.addFiguresColumn("Phone", c -> c.phone().formatLocal(), ColumnWidth.fixed(150));
    table.addTextColumn("Category", c -> c.category().label(), ColumnWidth.fixed(130));
    table.addTextColumn("County", c -> c.county().label(), ColumnWidth.fixed(150));
    table.addNodeColumn(
        "Status", ClientsPage::status, ClientSummary::active, ColumnWidth.fixed(120));
    table.setOnOpen(c -> pages.client(c.id()));
    Button register = new Button("Register a client");
    register.getStyleClass().add("primary");
    register.setOnAction(e -> register());
    table.setEmptyState(
        "No clients yet", "Register your first client to start quoting boreholes.", register);
    CheckBox inactive = new CheckBox("Show inactive");
    inactive.setId("showInactive");
    inactive.selectedProperty().bindBidirectional(model.showInactiveProperty());
    table.getActions().add(inactive);
    VBox.setVgrow(table, Priority.ALWAYS);

    PageHeader header = PageHeader.forTable("Everyone you have quoted or drilled for", table);
    add.getStyleClass().add("primary");
    add.setId("newClient");
    add.setAccessibleHelp("Ctrl+N");
    add.setOnAction(e -> register());
    header.getActions().add(add);

    root = new VBox(header, table);
    root.getStyleClass().addAll("page", "clients-page");
    // Ctrl+N works anywhere in the window while this page is showing, not only inside it.
    Runnable shortcut =
        () -> {
          if (!nav.overlays().isDialogOpen()) {
            register();
          }
        };
    root.sceneProperty()
        .addListener(
            (obs, old, now) -> {
              if (old != null && old.getAccelerators().get(NEW) == shortcut) {
                old.getAccelerators().remove(NEW);
              }
              if (now != null) {
                now.getAccelerators().put(NEW, shortcut);
              }
            });
    model.stateProperty().addListener((obs, old, now) -> render());
    model.load();
    render();
  }

  public Node root() {
    return InitialFocus.attach(Refreshable.attach(root, this), this);
  }

  @Override
  public Node initialFocusTarget() {
    return switch (model.stateProperty().get()) {
      case LOADING -> null;
      case READY -> table.searchField().isVisible() ? table.searchField() : add;
      case ERROR -> add;
    };
  }

  public ClientsViewModel model() {
    return model;
  }

  @Override
  public void refresh() {
    model.load();
  }

  private void render() {
    switch (model.stateProperty().get()) {
      case LOADING -> table.setLoading(true);
      case ERROR -> table.setError(model.error(), model::load);
      case READY -> table.setItems(model.rows());
    }
  }

  private void register() {
    nav.overlays()
        .showDialog(
            ClientDialog.register(
                backend,
                saved -> {
                  announce(saved);
                  model.load();
                }));
  }

  private void announce(Client saved) {
    nav.overlays().success("Client registered", saved.code().value() + " · " + saved.name());
  }

  private static Node status(ClientSummary c) {
    return c.active()
        ? new StatusBadge("Active", StatusBadge.Tone.SUCCESS)
        : new StatusBadge("Inactive", StatusBadge.Tone.NEUTRAL);
  }
}
