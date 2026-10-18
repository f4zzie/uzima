package com.uzima.ui.component;

import static com.uzima.ui.component.MoneyFieldTest.robotFx;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.money.Money;
import com.uzima.ui.UiTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
class DataTableTest {

  record Client(String code, String name, String county, Money balance) {}

  private static final List<Client> CLIENTS =
      List.of(
          new Client("UZM-DOM-0001", "Wanjiru Kamau", "Kiambu", Money.of(0)),
          new Client("UZM-COM-0001", "Baraka Dairy Ltd", "Nakuru", Money.of(600000)),
          new Client("UZM-IND-0001", "Mombasa Cement Works", "Mombasa", Money.of(125000)),
          new Client("UZM-DOM-0002", "Otieno Ochieng", "Kisumu", Money.of(42000)));

  private DataTable<Client> table;
  private TableColumn<Client, Money> balance;
  private final List<Client> opened = new ArrayList<>();

  @Start
  void start(Stage stage) {
    table = new DataTable<>();
    table.setItemNouns("client", "clients");
    table.setSearchMatcher(DataTable.containsIn(Client::code, Client::name, Client::county));
    table.addTextColumn("ID", Client::code);
    table.addTextColumn("Name", Client::name);
    table.addNodeColumn(
        "County", c -> new StatusBadge(c.county(), StatusBadge.Tone.NEUTRAL), Client::county);
    balance = table.addMoneyColumn("Balance", Client::balance);
    table.setOnOpen(opened::add);
    table.setEmptyState("No clients yet", "Register your first client.", null);
    table.setItems(CLIENTS);
    stage.setScene(UiTheme.apply(new Scene(new VBox(table), 900, 500)));
    stage.show();
  }

  @Test
  void searchFiltersCaseInsensitivelyAndUpdatesTheCount() {
    search("NAKURU");
    assertThat(table.getVisibleItems())
        .extracting(Client::name)
        .containsExactly("Baraka Dairy Ltd");
    assertThat(countText()).isEqualTo("1 of 4 clients");
    search("");
    assertThat(countText()).isEqualTo("4 clients");
  }

  @Test
  void sortingStillWorksWhileFiltered() {
    search("uzm-");
    robotFx(
        () -> {
          balance.setSortType(TableColumn.SortType.DESCENDING);
          table.tableView().getSortOrder().setAll(balance);
        });
    assertThat(table.getVisibleItems())
        .extracting(Client::name)
        .containsExactly(
            "Baraka Dairy Ltd", "Mombasa Cement Works", "Otieno Ochieng", "Wanjiru Kamau");
    search("dom");
    assertThat(table.getVisibleItems())
        .extracting(Client::name)
        .containsExactly("Otieno Ochieng", "Wanjiru Kamau");
  }

  @Test
  void noMatchesIsDifferentFromEmpty() {
    search("Garissa");
    EmptyState placeholder = (EmptyState) table.tableView().getPlaceholder();
    assertThat(placeholder.getTitle()).isEqualTo("No results for “Garissa”");

    robotFx(() -> table.setItems(List.of()));
    assertThat(placeholder.getTitle()).isEqualTo("No clients yet");
    assertThat(table.searchField().isVisible()).isFalse();
  }

  @Test
  void loadingAndErrorReplaceTheRows() {
    AtomicInteger retries = new AtomicInteger();
    EmptyState placeholder = (EmptyState) table.tableView().getPlaceholder();
    robotFx(() -> table.setLoading(true));
    assertThat(table.tableView().getItems()).isEmpty();
    assertThat(placeholder.getTitle()).isEqualTo("Loading…");
    assertThat(table.searchField().isDisabled()).isTrue();

    robotFx(() -> table.setError("The database is not reachable.", retries::incrementAndGet));
    assertThat(placeholder.getText()).isEqualTo("The database is not reachable.");
    robotFx(
        () ->
            placeholder.getChildren().stream()
                .filter(n -> n instanceof javafx.scene.control.Button)
                .map(javafx.scene.control.Button.class::cast)
                .findFirst()
                .orElseThrow()
                .fire());
    assertThat(retries).hasValue(1);

    robotFx(() -> table.setItems(CLIENTS));
    assertThat(table.tableView().getItems()).hasSize(4);
  }

  @Test
  void keyboardSearchAndOpen(FxRobot robot) {
    robot.clickOn(table.tableView());
    robot.press(KeyCode.CONTROL, KeyCode.F).release(KeyCode.F, KeyCode.CONTROL);
    assertThat(table.searchField().isFocused()).isTrue();

    robot.write("kisumu");
    assertThat(table.getVisibleItems()).hasSize(1);
    robot.type(KeyCode.ESCAPE);
    assertThat(table.searchField().getText()).isEmpty();

    robot.write("baraka");
    robot.type(KeyCode.DOWN);
    assertThat(table.tableView().isFocused()).isTrue();
    robot.type(KeyCode.ENTER);
    assertThat(opened).extracting(Client::name).containsExactly("Baraka Dairy Ltd");
  }

  @Test
  void doubleClickOpensTheRow(FxRobot robot) {
    // A real two-click event on the row; robot double-clicks can split into two single clicks on a
    // busy machine.
    TableRow<?> row =
        robot.lookup(".table-row-cell").queryAllAs(TableRow.class).stream()
            .filter(
                r -> r.getItem() != null && ((Client) r.getItem()).name().equals("Otieno Ochieng"))
            .findFirst()
            .orElseThrow();
    robotFx(
        () ->
            Event.fireEvent(
                row,
                new MouseEvent(
                    MouseEvent.MOUSE_CLICKED,
                    5,
                    5,
                    5,
                    5,
                    MouseButton.PRIMARY,
                    2,
                    false,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false,
                    true,
                    false,
                    false,
                    null)));
    assertThat(opened).extracting(Client::name).containsExactly("Otieno Ochieng");

    robotFx(
        () ->
            Event.fireEvent(
                row,
                new MouseEvent(
                    MouseEvent.MOUSE_CLICKED,
                    5,
                    5,
                    5,
                    5,
                    MouseButton.PRIMARY,
                    1,
                    false,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false,
                    true,
                    false,
                    false,
                    null)));
    assertThat(opened).hasSize(1);
  }

  @Test
  void moneyIsRightAlignedAndFormatted(FxRobot robot) {
    assertThat(robot.lookup("600,000.00").queryLabeled().getStyleClass())
        .contains("numeric", "text-numeric");
  }

  private void search(String text) {
    robotFx(() -> table.searchField().setText(text));
  }

  private String countText() {
    return ((javafx.scene.control.Label) table.lookup(".data-table-count")).getText();
  }
}
