package com.uzima.ui.dev;

import com.uzima.domain.common.Metres;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.service.JobSummary;
import com.uzima.ui.SampleData;
import com.uzima.ui.component.ColumnWidth;
import com.uzima.ui.component.DataTable;
import com.uzima.ui.component.FormField;
import com.uzima.ui.component.MetresField;
import com.uzima.ui.component.MoneyField;
import com.uzima.ui.component.StatusBadge;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Money and metres fields, status badges and the data table in each of its states. */
final class DataComponentsSection implements StyleGuideSection {

  /** The jobs from the shared snapshot data, so the table matches every other screenshot. */
  static List<JobSummary> jobs() {
    return SampleData.shared().jobSummaries();
  }

  @Override
  public String title() {
    return "Data components";
  }

  @Override
  public String description() {
    return "Amount and length inputs, status badges, and the searchable table with its empty,"
        + " no-results and loading states.";
  }

  @Override
  public Node build() {
    return new VBox(
        24,
        fields(),
        badges(),
        caption("Table with rows"),
        table(jobs(), ""),
        caption("Empty"),
        emptyTable(),
        caption("No results"),
        table(jobs(), "Garissa"),
        caption("Loading"),
        loadingTable());
  }

  private static Node fields() {
    MoneyField amount = new MoneyField();
    amount.setValue(Money.of("600000.00"));
    FormField amountField = new FormField("Amount received", amount);
    amountField.setHelp("JOB-2026-00001 balance: KSh 600,000.00");

    MetresField depth = new MetresField();
    depth.setValue(Metres.of("150.00"));
    FormField depthField = new FormField("Drilling depth", depth);

    MoneyField bad = new MoneyField();
    bad.textField().setText("0");
    FormField badField = new FormField("Unit price", bad);
    badField.setError(bad.getProblem());

    MetresField empty = new MetresField();
    FormField emptyField = new FormField("Lift height", empty);
    emptyField.setHelp("Height the pump lifts water, in metres.");

    GridPane grid = new GridPane();
    grid.getStyleClass().add("style-guide-grid");
    grid.addRow(0, width(amountField), width(depthField));
    grid.addRow(1, width(badField), width(emptyField));
    return grid;
  }

  private static Node badges() {
    HBox jobs = new HBox(8);
    for (JobStatus s : JobStatus.values()) {
      jobs.getChildren().add(StatusBadge.of(s));
    }
    HBox payments = new HBox(8);
    for (PaymentStatus s : PaymentStatus.values()) {
      payments.getChildren().add(StatusBadge.of(s));
    }
    return new VBox(
        12, ButtonsSection.row("Job status", jobs), ButtonsSection.row("Payment", payments));
  }

  static DataTable<JobSummary> table(List<JobSummary> rows, String query) {
    DataTable<JobSummary> table = new DataTable<>();
    table.setItemNouns("job", "jobs");
    table.setSearchPrompt("Search by job, client or client ID");
    table.setSearchMatcher(
        DataTable.containsIn(
            j -> j.number().value(), JobSummary::clientName, j -> j.clientCode().value()));
    table.addFiguresColumn("Job", j -> j.number().value(), ColumnWidth.fixed(150));
    table.addTextColumn("Client", JobSummary::clientName, ColumnWidth.flex(200));
    table.addNodeColumn(
        "Status", j -> StatusBadge.of(j.status()), JobSummary::status, ColumnWidth.fixed(140));
    table.addMoneyColumn("Total (KSh)", JobSummary::grandTotal);
    table.addMoneyColumn("Balance (KSh)", JobSummary::balance);
    table.addNodeColumn(
        "Payment",
        j -> StatusBadge.of(j.paymentStatus()),
        JobSummary::paymentStatus,
        ColumnWidth.fixed(140));
    Button add = new Button("New job");
    add.getStyleClass().add("primary");
    table.getActions().add(add);
    table.setItems(rows);
    table.searchField().setText(query);
    table.setPrefHeight(rows.isEmpty() || !query.isEmpty() ? 260 : 360);
    return table;
  }

  private static Node emptyTable() {
    DataTable<JobSummary> table = table(List.of(), "");
    Button add = new Button("Register a client");
    add.getStyleClass().add("primary");
    table.getActions().clear();
    table.setItemNouns("client", "clients");
    table.setEmptyState(
        "No clients yet", "Register your first client to start quoting boreholes.", add);
    return table;
  }

  private static Node loadingTable() {
    DataTable<JobSummary> table = table(jobs(), "");
    table.setLoading(true);
    table.setPrefHeight(220);
    return table;
  }

  private static Label caption(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("text-overline");
    return label;
  }

  private static FormField width(FormField field) {
    field.setPrefWidth(320);
    return field;
  }
}
