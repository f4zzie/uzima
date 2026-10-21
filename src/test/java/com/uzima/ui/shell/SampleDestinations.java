package com.uzima.ui.shell;

import com.uzima.domain.user.Permission;
import com.uzima.service.ClientSummary;
import com.uzima.ui.SampleData;
import com.uzima.ui.component.ColumnWidth;
import com.uzima.ui.component.DataTable;
import com.uzima.ui.component.PageHeader;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Test-only sidebar entries with realistic sample pages, used to show the shell's look in snapshots
 * before the real screens exist. Never registered by the app.
 */
public final class SampleDestinations {

  private SampleDestinations() {}

  public static List<Destination> all() {
    return List.of(
        new Destination("dashboard", "Dashboard", Icon.DASHBOARD, null, nav -> dashboard()),
        new Destination("clients", "Clients", Icon.CLIENTS, null, nav -> clients()),
        new Destination("jobs", "Jobs", Icon.JOBS, null, nav -> simple("Jobs")),
        new Destination(
            "payments",
            "Payments",
            Icon.PAYMENTS,
            Permission.RECORD_PAYMENTS,
            nav -> simple("Payments")),
        new Destination(
            "reports", "Reports", Icon.REPORTS, Permission.VIEW_REPORTS, nav -> simple("Reports")),
        new Destination(
            "catalog", "Catalog", Icon.CATALOG, Permission.EDIT_CATALOG, nav -> simple("Catalog")),
        new Destination(
            "users", "Users", Icon.USERS, Permission.MANAGE_USERS, nav -> simple("Users")),
        new Destination(
            "audit",
            "Audit log",
            Icon.AUDIT,
            Permission.VIEW_AUDIT_LOG,
            nav -> simple("Audit log")),
        new Destination(
            "settings",
            "Settings",
            Icon.SETTINGS,
            Permission.EDIT_SETTINGS,
            nav -> simple("Settings")));
  }

  private static Node dashboard() {
    HBox stats =
        new HBox(
            stat("Open jobs", "4", "2 awaiting survey"),
            stat("Collected in October", "KSh 398,120.00", "2 payments"),
            stat("Refunds due", "KSh 200,000.00", "JOB-2026-00004 · St. Mary's Girls School"));
    stats.getStyleClass().add("stack-16");
    stats.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
    return page(new PageHeader("Friday 16 October 2026"), stats);
  }

  /** The clients from the shared snapshot data, in the real table component. */
  private static Node clients() {
    DataTable<ClientSummary> table = new DataTable<>();
    table.setItemNouns("client", "clients");
    table.setSearchPrompt("Search by name, client ID or phone");
    table.setSearchMatcher(
        DataTable.containsIn(
            c -> c.code().value(), ClientSummary::name, c -> c.phone().formatLocal()));
    table.addFiguresColumn("Client ID", c -> c.code().value(), ColumnWidth.fixed(160));
    table.addTextColumn("Name", ClientSummary::name, ColumnWidth.flex(220));
    table.addTextColumn("Category", c -> c.category().label(), ColumnWidth.fixed(130));
    table.addTextColumn("County", c -> c.county().label(), ColumnWidth.fixed(150));
    table.addFiguresColumn("Phone", c -> c.phone().formatLocal(), ColumnWidth.fixed(170));
    table.setItems(SampleData.shared().clientSummaries());
    VBox.setVgrow(table, Priority.ALWAYS);
    PageHeader header = PageHeader.forTable("Everyone you have quoted or drilled for", table);
    Button add = new Button("New client");
    add.getStyleClass().add("primary");
    header.getActions().add(add);
    return page(header, table);
  }

  private static Node simple(String title) {
    Label body = new Label("Sample page for the shell preview.");
    body.getStyleClass().add("text-muted");
    VBox card = new VBox(body);
    card.getStyleClass().add("card");
    return page(new PageHeader(title + " sample"), card);
  }

  private static VBox page(PageHeader header, Node body) {
    VBox page = new VBox(header, body);
    page.getStyleClass().add("page");
    return page;
  }

  private static VBox stat(String label, String value, String note) {
    Label l = new Label(label.toUpperCase(java.util.Locale.ROOT));
    l.getStyleClass().add("text-overline");
    Label v = new Label(value);
    v.getStyleClass().addAll("text-headline", "figures-strong");
    Label n = new Label(note);
    n.getStyleClass().add("text-caption");
    VBox card = new VBox(l, v, n);
    card.getStyleClass().addAll("card", "stack-8");
    card.setMaxWidth(Double.MAX_VALUE);
    card.setPrefWidth(1); // equal widths: the row shares its space evenly
    return card;
  }
}
