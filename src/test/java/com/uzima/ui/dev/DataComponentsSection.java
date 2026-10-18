package com.uzima.ui.dev;

import com.uzima.domain.common.Metres;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.PaymentStatus;
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

  /** A sample row; the real screens use service summaries. */
  record SampleJob(
      String number,
      String client,
      String county,
      JobStatus status,
      Money total,
      PaymentStatus payment) {}

  static final List<SampleJob> JOBS =
      List.of(
          new SampleJob(
              "JOB-2026-00041",
              "Wanjiru Kamau",
              "Kiambu",
              JobStatus.COMPLETED,
              Money.of("298120.00"),
              PaymentStatus.PAID),
          new SampleJob(
              "JOB-2026-00042",
              "Baraka Dairy Ltd",
              "Nakuru",
              JobStatus.IN_PROGRESS,
              Money.of("684400.00"),
              PaymentStatus.PARTIALLY_PAID),
          new SampleJob(
              "JOB-2026-00043",
              "Otieno Ochieng",
              "Kisumu",
              JobStatus.APPROVED,
              Money.of("231840.00"),
              PaymentStatus.NOT_PAID),
          new SampleJob(
              "JOB-2026-00044",
              "Mwangaza Flower Farm",
              "Laikipia",
              JobStatus.PENDING_SURVEY,
              Money.of("1412560.00"),
              PaymentStatus.NOT_PAID),
          new SampleJob(
              "JOB-2026-00045",
              "Chebet Kiprono",
              "Uasin Gishu",
              JobStatus.CANCELLED,
              Money.of("189080.00"),
              PaymentStatus.PARTIALLY_PAID),
          new SampleJob(
              "JOB-2026-00046",
              "Mombasa Cement Works",
              "Mombasa",
              JobStatus.COMPLETED,
              Money.of("2045800.00"),
              PaymentStatus.PAID));

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
        table(JOBS, ""),
        caption("Empty"),
        emptyTable(),
        caption("No results"),
        table(JOBS, "Garissa"),
        caption("Loading"),
        loadingTable());
  }

  private static Node fields() {
    MoneyField amount = new MoneyField();
    amount.setValue(Money.of("684400.00"));
    FormField amountField = new FormField("Amount received", amount);
    amountField.setHelp("Up to the balance of KSh 600,000.00");

    MetresField depth = new MetresField();
    depth.setValue(Metres.of("150.50"));
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

  static DataTable<SampleJob> table(List<SampleJob> rows, String query) {
    DataTable<SampleJob> table = new DataTable<>();
    table.setItemNouns("job", "jobs");
    table.setSearchPrompt("Search jobs, clients or counties");
    table.setSearchMatcher(
        DataTable.containsIn(SampleJob::number, SampleJob::client, SampleJob::county));
    table.addTextColumn("Job", SampleJob::number);
    table.addTextColumn("Client", SampleJob::client);
    table.addTextColumn("County", SampleJob::county);
    table.addNodeColumn("Status", j -> StatusBadge.of(j.status()), SampleJob::status);
    table.addMoneyColumn("Total (KSh)", SampleJob::total);
    table.addNodeColumn("Payment", j -> StatusBadge.of(j.payment()), SampleJob::payment);
    Button add = new Button("New job");
    add.getStyleClass().add("primary");
    table.getActions().add(add);
    table.setItems(rows);
    table.searchField().setText(query);
    table.setPrefHeight(rows.isEmpty() || !query.isEmpty() ? 260 : 360);
    return table;
  }

  private static Node emptyTable() {
    DataTable<SampleJob> table = table(List.of(), "");
    Button add = new Button("Register a client");
    add.getStyleClass().add("primary");
    table.getActions().clear();
    table.setItemNouns("client", "clients");
    table.setEmptyState(
        "No clients yet", "Register your first client to start quoting boreholes.", add);
    return table;
  }

  private static Node loadingTable() {
    DataTable<SampleJob> table = table(JOBS, "");
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
