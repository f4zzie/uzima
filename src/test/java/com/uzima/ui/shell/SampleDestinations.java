package com.uzima.ui.shell;

import com.uzima.domain.user.Permission;
import com.uzima.ui.component.DataTable;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Test-only sidebar entries with realistic sample pages, used to show the shell's look in snapshots
 * before the real screens exist. Never registered by the app.
 */
public final class SampleDestinations {

  private SampleDestinations() {}

  public static List<Destination> all() {
    return List.of(
        new Destination(
            "dashboard", "Dashboard", Icon.DASHBOARD, null, SampleDestinations::dashboard),
        new Destination("clients", "Clients", Icon.CLIENTS, null, SampleDestinations::clients),
        new Destination("jobs", "Jobs", Icon.JOBS, null, () -> simple("Jobs")),
        new Destination(
            "payments",
            "Payments",
            Icon.PAYMENTS,
            Permission.RECORD_PAYMENTS,
            () -> simple("Payments")),
        new Destination(
            "reports", "Reports", Icon.REPORTS, Permission.VIEW_REPORTS, () -> simple("Reports")),
        new Destination(
            "catalog", "Catalog", Icon.CATALOG, Permission.EDIT_CATALOG, () -> simple("Catalog")),
        new Destination(
            "users", "Users", Icon.USERS, Permission.MANAGE_USERS, () -> simple("Users")),
        new Destination(
            "audit", "Audit log", Icon.AUDIT, Permission.VIEW_AUDIT_LOG, () -> simple("Audit log")),
        new Destination(
            "settings",
            "Settings",
            Icon.SETTINGS,
            Permission.EDIT_SETTINGS,
            () -> simple("Settings")));
  }

  private static Node dashboard() {
    HBox stats =
        new HBox(
            stat("Outstanding balances", "KSh 1,284,600.00", "14 jobs"),
            stat("Collected this month", "KSh 2,040,000.00", "23 payments"),
            stat("Jobs in progress", "9", "3 awaiting survey"));
    stats.getStyleClass().add("stack-16");
    stats.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
    return page("Overview", "Week of 12 October 2026", stats);
  }

  private static Node clients() {
    DataTable<String[]> table = new DataTable<>();
    table.setItemNouns("client", "clients");
    table.setSearchPrompt("Search by name, ID, county or phone");
    table.setSearchMatcher(DataTable.containsIn(r -> String.join(" ", r)));
    String[] headings = {"Client ID", "Name", "Category", "County", "Phone"};
    for (int i = 0; i < headings.length; i++) {
      int column = i;
      table.addTextColumn(headings[i], row -> row[column]).setPrefWidth(i == 1 ? 260 : 160);
    }
    table.setItems(
        List.of(
            new String[] {
              "UZM-DOM-0007", "Wanjiru Kamau", "Domestic", "Kiambu", "+254 712 345 678"
            },
            new String[] {
              "UZM-COM-0003", "Baraka Hotels Ltd", "Commercial", "Nakuru", "+254 733 100 200"
            },
            new String[] {
              "UZM-IND-0001", "Athi River Cement", "Industrial", "Machakos", "+254 722 900 111"
            },
            new String[] {
              "UZM-DOM-0006", "Otieno Ochieng", "Domestic", "Kisumu", "+254 701 222 333"
            },
            new String[] {
              "UZM-COM-0002", "St. Mary's School", "Commercial", "Nyeri", "+254 720 555 010"
            }));
    VBox.setVgrow(table, Priority.ALWAYS);
    Button add = new Button("New client");
    add.getStyleClass().add("primary");
    return page("All clients", "Everyone you have quoted or drilled for", table, add);
  }

  private static Node simple(String title) {
    Label body = new Label("Sample page for the shell preview.");
    body.getStyleClass().add("text-muted");
    VBox card = new VBox(body);
    card.getStyleClass().add("card");
    return page(title, "", card);
  }

  private static VBox page(String title, String subtitle, Node body, Node... actions) {
    Label heading = new Label(title);
    heading.getStyleClass().add("text-title");
    Label sub = new Label(subtitle);
    sub.getStyleClass().add("text-muted");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(new VBox(2, heading, sub), spacer);
    header.getChildren().addAll(actions);
    header.getStyleClass().add("page-header");
    VBox page = new VBox(header, body);
    page.getStyleClass().add("page");
    return page;
  }

  private static VBox stat(String label, String value, String note) {
    Label l = new Label(label.toUpperCase(java.util.Locale.ROOT));
    l.getStyleClass().add("text-overline");
    Label v = new Label(value);
    v.getStyleClass().add("text-headline");
    Label n = new Label(note);
    n.getStyleClass().add("text-caption");
    VBox card = new VBox(l, v, n);
    card.getStyleClass().addAll("card", "stack-8");
    card.setMaxWidth(Double.MAX_VALUE);
    card.setPrefWidth(1); // equal widths: the row shares its space evenly
    return card;
  }
}
