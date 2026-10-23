package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.job.JobStatus;
import com.uzima.service.JobSummary;
import com.uzima.ui.Backend;
import com.uzima.ui.Pages;
import com.uzima.ui.component.ColumnWidth;
import com.uzima.ui.component.ConfirmDialog;
import com.uzima.ui.component.DataTable;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.PageHeader;
import com.uzima.ui.component.StatePane;
import com.uzima.ui.component.StatusBadge;
import com.uzima.ui.shell.Navigator;
import com.uzima.ui.shell.Refreshable;
import java.util.List;
import java.util.OptionalLong;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * One client: contact details and borehole location, totals across their jobs, and the jobs
 * themselves. Pushed onto the shell with the client's name as its title.
 */
public final class ClientDetailPage implements Refreshable {

  private final Backend backend;
  private final Navigator nav;
  private final Pages pages;
  private final ClientDetailViewModel model;
  private final VBox content = new VBox();
  private final StatePane state = new StatePane(content);
  private final ScrollPane root = new ScrollPane(state);
  private final DataTable<JobSummary> jobs = new DataTable<>();
  private PageHeader header;
  private String title = PLACEHOLDER_TITLE;

  /** The top-bar title used until the client has loaded. */
  static final String PLACEHOLDER_TITLE = "Client";

  public ClientDetailPage(Backend backend, Navigator nav, Pages pages, long clientId) {
    this.backend = backend;
    this.nav = nav;
    this.pages = pages;
    this.model = new ClientDetailViewModel(backend, clientId);
    content.getStyleClass().addAll("page", "client-detail");
    root.setFitToWidth(true);
    root.getStyleClass().add("client-detail-scroll");
    buildJobsTable();
    model.stateProperty().addListener((obs, old, now) -> render());
    model.load();
    render();
  }

  /**
   * Builds the page and pushes it onto the shell. The top bar says "Client" until the client has
   * loaded, then shows the client's name (and follows renames).
   */
  public static ClientDetailPage show(Backend backend, Navigator nav, Pages pages, long clientId) {
    ClientDetailPage page = new ClientDetailPage(backend, nav, pages, clientId);
    nav.push(PLACEHOLDER_TITLE, page.root());
    return page;
  }

  public Node root() {
    return root;
  }

  public ClientDetailViewModel model() {
    return model;
  }

  @Override
  public void refresh() {
    model.load();
  }

  private void render() {
    switch (model.stateProperty().get()) {
      case LOADING -> {
        if (model.detailProperty().get() == null) {
          state.showLoading("Loading client…");
        }
      }
      case ERROR -> state.showError(model.errorProperty().get(), model::load);
      case READY -> {
        boolean first = content.getChildren().isEmpty();
        show(model.detailProperty().get());
        state.showContent();
        if (first) {
          // Keyboard users land on the page's first action, not back in the sidebar.
          focusFirstAction();
        }
      }
    }
  }

  private void show(ClientDetailViewModel.Detail detail) {
    Client client = detail.client();
    if (!title.equals(client.name()) && root.getScene() != null) {
      // The page is on top whenever it loads (first open, after an edit, or on coming back).
      nav.replace(client.name(), root);
      title = client.name();
    }
    jobs.setItems(detail.jobs());
    content.getChildren().setAll(header(client), totals(detail.totals()), body(client));
  }

  /** Focuses the primary action (the last one in the header), or the jobs table if none. */
  private void focusFirstAction() {
    focusFirstAction(0);
  }

  private void focusFirstAction(int attempt) {
    List<Node> actions = header == null ? List.of() : header.getActions();
    Node target = actions.isEmpty() ? jobs.tableView() : actions.get(actions.size() - 1);
    Scene scene = target.getScene();
    Window window = scene != null ? scene.getWindow() : null;
    boolean inShowingScene = scene != null && window != null && window.isShowing();
    if (!inShowingScene) {
      // Not in a showing scene yet; focus once it is.
      target.sceneProperty().addListener(new ChangeListener<Scene>() {
        @Override
        public void changed(ObservableValue<? extends Scene> obs, Scene old, Scene now) {
          if (now != null) {
            target.sceneProperty().removeListener(this);
            Platform.runLater(ClientDetailPage.this::focusFirstAction);
          }
        }
      });
      return;
    }
    target.requestFocus();
    if (!target.isFocused() && attempt < 15) {
      Platform.runLater(() -> focusFirstAction(attempt + 1));
    }
  }

  private PageHeader header(Client client) {
    header =
        new PageHeader(
            client.code().value()
                + " · "
                + client.category().label()
                + " · Registered "
                + DisplayFormats.DATE.format(client.createdAt().atZone(DisplayFormats.ZONE)));
    if (model.canDeactivate()) {
      header.getActions().add(client.active() ? deactivateButton(client) : reactivateButton());
    }
    if (model.canEdit()) {
      Button edit = new Button("Edit");
      edit.setId("editClient");
      edit.setOnAction(e -> edit(client));
      header.getActions().add(edit);
    }
    if (model.canCreateJobs() && client.active()) {
      Button newJob = new Button("New job");
      newJob.getStyleClass().add("primary");
      newJob.setId("newJob");
      newJob.setOnAction(e -> pages.newJob(OptionalLong.of(client.id())));
      header.getActions().add(newJob);
    }
    return header;
  }

  private Node totals(ClientTotals t) {
    HBox row =
        new HBox(
            stat("Jobs", String.valueOf(t.jobs()), "quotes and jobs, any status"),
            stat("Total quoted", t.quoted().format(), "excluding cancelled jobs"),
            stat("Paid", t.paid().format(), "net of any refunds"),
            stat("Balance", t.balance().format(), "still to pay"));
    if (t.refundDue().isPositive()) {
      VBox refund = stat("Refund due", t.refundDue().format(), "received on cancelled jobs");
      refund.getStyleClass().add("client-stat-refund");
      row.getChildren().add(refund);
    }
    row.getStyleClass().add("client-stats");
    row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
    return row;
  }

  /** Contact details across the top, then the client's jobs at full width. */
  private Node body(Client client) {
    Label jobsTitle = new Label("Jobs");
    jobsTitle.getStyleClass().add("text-subtitle");
    VBox jobsSection = new VBox(jobsTitle, jobs);
    jobsSection.getStyleClass().add("stack-12");
    VBox body = new VBox(contactCard(client), jobsSection);
    body.getStyleClass().add("client-detail-body");
    return body;
  }

  private Node contactCard(Client client) {
    ClientDetails d = client.details();
    GridPane grid = new GridPane();
    grid.getStyleClass().add("client-facts");
    Label status = new Label();
    status.setGraphic(
        client.active()
            ? new StatusBadge("Active", StatusBadge.Tone.SUCCESS)
            : new StatusBadge("Inactive", StatusBadge.Tone.NEUTRAL));
    // Two columns of facts: who to call on the left, where the borehole is on the right.
    grid.addRow(
        0,
        term("Phone"),
        value(d.phone().formatLocal(), true),
        term("County"),
        value(d.county().label(), false));
    grid.addRow(
        1,
        term("Email"),
        value(d.email().map(Object::toString).orElse("—"), false),
        term("Address"),
        value(d.address().isBlank() ? "—" : d.address(), false));
    grid.addRow(
        2,
        term("Status"),
        status,
        term("Borehole GPS"),
        value(d.location().map(Object::toString).orElse("Not recorded"), d.location().isPresent()));
    ColumnConstraints termColumn = new ColumnConstraints();
    termColumn.setMinWidth(Region.USE_PREF_SIZE);
    ColumnConstraints valueColumn = new ColumnConstraints();
    valueColumn.setPercentWidth(36);
    grid.getColumnConstraints().addAll(termColumn, valueColumn, termColumn, valueColumn);
    Label title = new Label("Contact and location");
    title.getStyleClass().add("text-subtitle");
    VBox card = new VBox(title, grid);
    card.getStyleClass().addAll("card", "stack-16", "client-contact");
    if (d.hasLocationOutsideKenya()) {
      Label warning = new Label("This point is outside Kenya. Check the coordinates.");
      warning.getStyleClass().add("form-warning");
      warning.setWrapText(true);
      card.getChildren().add(warning);
    }
    return card;
  }

  private void buildJobsTable() {
    jobs.setItemNouns("job", "jobs");
    jobs.setSearchPrompt("Search this client's jobs");
    jobs.setSearchMatcher(DataTable.containsIn(j -> j.number().value()));
    jobs.addFiguresColumn("Job", j -> j.number().value(), ColumnWidth.fixed(150));
    jobs.addFiguresColumn(
        "Created",
        j -> DisplayFormats.DATE.format(j.createdAt().atZone(DisplayFormats.ZONE)),
        ColumnWidth.fixed(110));
    jobs.addNodeColumn(
        "Status", j -> StatusBadge.of(j.status()), JobSummary::status, ColumnWidth.fixed(140));
    jobs.addMoneyColumn("Total (KSh)", JobSummary::grandTotal);
    jobs.addMoneyColumn("Balance (KSh)", JobSummary::balance);
    jobs.addNodeColumn(
        "Payment", ClientDetailPage::payment, JobSummary::paymentStatus, ColumnWidth.fixed(140));
    jobs.setOnOpen(j -> pages.job(j.id()));
    jobs.setEmptyState("No jobs yet", "Quotes you create for this client appear here.", null);
    jobs.setPrefHeight(320);
    jobs.setMinHeight(260);
  }

  /**
   * The payment badge; a cancelled job owes nothing, so it shows the refund due (or nothing) rather
   * than a payment status that no longer applies.
   */
  private static Node payment(JobSummary job) {
    if (job.status() != JobStatus.CANCELLED) {
      return StatusBadge.of(job.paymentStatus());
    }
    return job.refundDue().isPositive()
        ? new StatusBadge("Refund due", StatusBadge.Tone.WARNING)
        : new Label("—");
  }

  private void edit(Client client) {
    nav.overlays()
        .showDialog(
            ClientDialog.edit(
                backend,
                client,
                saved -> {
                  nav.overlays()
                      .success("Client updated", saved.code().value() + " · " + saved.name());
                  model.load();
                }));
  }

  private Button deactivateButton(Client client) {
    Button b = new Button("Deactivate");
    b.setId("deactivateClient");
    b.setOnAction(
        e -> {
          ConfirmDialog confirm =
              new ConfirmDialog(
                      "Deactivate " + client.name() + "?",
                      "Inactive clients are hidden from the client list and can't be given new"
                          + " quotes. Their jobs and payments stay as they are, and you can"
                          + " reactivate them at any time.")
                  .confirmLabel("Deactivate", true)
                  .cancelLabel("Keep active");
          confirm.onConfirm(
              (shell, reason) -> {
                shell.setBusy(true);
                model.setActive(
                    false,
                    saved -> {
                      shell.close(DialogShell.Outcome.CONFIRMED);
                      nav.overlays()
                          .success(
                              "Client deactivated", saved.code().value() + " · " + saved.name());
                    },
                    error -> {
                      shell.setBusy(false);
                      shell.showError(message(error));
                    });
              });
          nav.overlays().showDialog(confirm.shell());
        });
    return b;
  }

  private Button reactivateButton() {
    Button b = new Button("Reactivate");
    b.setId("reactivateClient");
    b.setOnAction(
        e -> {
          b.setDisable(true);
          model.setActive(
              true,
              saved ->
                  nav.overlays()
                      .success("Client reactivated", saved.code().value() + " · " + saved.name()),
              error -> {
                b.setDisable(false);
                nav.overlays().error("Couldn't reactivate the client", message(error));
              });
        });
    return b;
  }

  private static String message(Throwable error) {
    return error instanceof DomainException rule
        ? rule.getMessage()
        : "Uzima couldn't save the change. Try again.";
  }

  private static VBox stat(String label, String value, String note) {
    Label l = new Label(label.toUpperCase(java.util.Locale.ROOT));
    l.getStyleClass().add("text-overline");
    Label v = new Label(value);
    v.getStyleClass().addAll("client-stat-value", "figures-strong");
    Label n = new Label(note);
    n.getStyleClass().add("text-caption");
    VBox card = new VBox(l, v, n);
    card.getStyleClass().addAll("card", "stack-4", "client-stat");
    card.setPrefWidth(1);
    card.setMaxWidth(Double.MAX_VALUE);
    return card;
  }

  private static Label term(String text) {
    Label l = new Label(text);
    l.getStyleClass().add("client-fact-term");
    l.setMinWidth(Region.USE_PREF_SIZE);
    return l;
  }

  private static Label value(String text, boolean figures) {
    Label l = new Label(text);
    l.getStyleClass().add("client-fact-value");
    if (figures) {
      l.getStyleClass().add("figures");
    }
    l.setWrapText(true);
    return l;
  }
}
