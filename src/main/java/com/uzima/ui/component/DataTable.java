package com.uzima.ui.component;

import com.uzima.domain.money.Money;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A searchable, sortable table with a toolbar and proper empty, no-results, loading and error
 * states. Searching filters the rows with a caller-supplied predicate; clicking headers still sorts
 * the filtered rows.
 *
 * <p>Keyboard: Ctrl+F (Cmd+F on macOS) focuses the search box, Esc in the search box clears it, and
 * Enter or a double-click on a row calls the {@link #setOnOpen open} handler.
 *
 * @param <T> the row type
 */
public class DataTable<T> extends VBox {

  private static final KeyCombination FIND =
      new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);

  private final ObservableList<T> items = FXCollections.observableArrayList();
  private final FilteredList<T> filtered = new FilteredList<>(items);
  private final SortedList<T> sorted = new SortedList<>(filtered);
  private final ObservableList<T> none = FXCollections.observableArrayList();
  private final TableView<T> table = new TableView<>(sorted);
  private final TextField search = new TextField();
  private final Button clearSearch = new Button("Clear");
  private final Label count = new Label();
  private final HBox actions = new HBox();
  private final EmptyState placeholder = new EmptyState();

  private BiPredicate<T, String> matcher = (row, query) -> true;
  private Consumer<T> onOpen = row -> {};
  private String singular = "row";
  private String plural = "rows";
  private String emptyTitle = "Nothing here yet";
  private String emptyText = "";
  private Node emptyAction;
  private State state = State.READY;
  private boolean showCount = true;

  private enum State {
    READY,
    LOADING,
    ERROR
  }

  public DataTable() {
    getStyleClass().add("data-table");
    sorted.comparatorProperty().bind(table.comparatorProperty());
    // Fixed columns keep their width, bounded ones stop at their max, flex columns take the rest.
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    table.setPlaceholder(placeholder);
    VBox.setVgrow(table, Priority.ALWAYS);

    search.getStyleClass().add("data-table-search");
    search.setPromptText("Search");
    search.setAccessibleText("Search");
    search.textProperty().addListener((obs, old, now) -> applyFilter());
    search.addEventHandler(
        KeyEvent.KEY_PRESSED,
        e -> {
          if (e.getCode() == KeyCode.ESCAPE && !search.getText().isEmpty()) {
            search.clear();
            e.consume();
          } else if (e.getCode() == KeyCode.DOWN && !table.getItems().isEmpty()) {
            table.requestFocus();
            e.consume();
          }
        });
    clearSearch.getStyleClass().addAll("ghost", "small");
    clearSearch.setAccessibleText("Clear search");
    clearSearch.setOnAction(e -> clearSearch());
    clearSearch.visibleProperty().bind(search.textProperty().isNotEmpty());
    clearSearch.managedProperty().bind(clearSearch.visibleProperty());
    count.getStyleClass().add("data-table-count");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    actions.getStyleClass().add("data-table-actions");
    HBox toolbar = new HBox(search, clearSearch, count, spacer, actions);
    toolbar.getStyleClass().add("data-table-toolbar");
    getChildren().addAll(toolbar, table);

    table.setRowFactory(t -> newRow());
    table.addEventHandler(
        KeyEvent.KEY_PRESSED,
        e -> {
          if (e.getCode() == KeyCode.ENTER && selected() != null) {
            onOpen.accept(selected());
            e.consume();
          }
        });
    table
        .focusedProperty()
        .addListener(
            (obs, was, focused) -> {
              if (focused && table.getSelectionModel().isEmpty() && !table.getItems().isEmpty()) {
                table.getSelectionModel().selectFirst();
                table.getFocusModel().focus(0);
              }
            });
    addEventFilter(
        KeyEvent.KEY_PRESSED,
        e -> {
          if (FIND.match(e)) {
            focusSearch();
            e.consume();
          }
        });
    items.addListener((javafx.collections.ListChangeListener<T>) c -> refresh());
    refresh();
  }

  // ------------------------------------------------------------------ configuration

  /** How rows match the search text; the query is passed trimmed and lower-cased. */
  public void setSearchMatcher(BiPredicate<T, String> matcher) {
    this.matcher = Objects.requireNonNull(matcher);
    applyFilter();
  }

  /** Builds a matcher that looks for the query in any of the given text fields, ignoring case. */
  @SafeVarargs
  public static <T> BiPredicate<T, String> containsIn(Function<T, String>... fields) {
    return (row, query) -> {
      for (Function<T, String> field : fields) {
        String value = field.apply(row);
        if (value != null && value.toLowerCase(Locale.ROOT).contains(query)) {
          return true;
        }
      }
      return false;
    };
  }

  /** Nouns for the result count, e.g. ("client", "clients") gives "3 of 12 clients". */
  public void setItemNouns(String singular, String plural) {
    this.singular = singular;
    this.plural = plural;
    refresh();
  }

  public void setSearchPrompt(String prompt) {
    search.setPromptText(prompt);
    search.setAccessibleText(prompt);
  }

  /** What to show when there are no rows at all, with an optional call to action. */
  public void setEmptyState(String title, String text, Node action) {
    this.emptyTitle = title;
    this.emptyText = text;
    this.emptyAction = action;
    refresh();
  }

  /** Called with the row the user opens by double-click or Enter. */
  public void setOnOpen(Consumer<T> onOpen) {
    this.onOpen = Objects.requireNonNull(onOpen);
  }

  /**
   * The row count as shown ("6 jobs", "2 of 6 jobs"), so a {@link PageHeader} can show it instead
   * of the toolbar; see {@link #setShowCount}.
   */
  public javafx.beans.property.ReadOnlyStringProperty countTextProperty() {
    return count.textProperty();
  }

  /** Whether the toolbar shows the count itself; turn off when a page header shows it. */
  public void setShowCount(boolean show) {
    showCount = show;
    refresh();
  }

  /** Buttons shown at the right of the toolbar, e.g. "New client". */
  public ObservableList<Node> getActions() {
    return actions.getChildren();
  }

  // ------------------------------------------------------------------ columns

  /** A plain text column, e.g. a name (usually {@link ColumnWidth#flex flex}). */
  public TableColumn<T, String> addTextColumn(
      String title, Function<T, String> value, ColumnWidth width) {
    TableColumn<T, String> column = new TableColumn<>(title);
    column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(value.apply(c.getValue())));
    column.setComparator(Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    width.applyTo(column);
    table.getColumns().add(column);
    return column;
  }

  /**
   * A text column set in tabular figures, for values whose digits should line up down the column:
   * client IDs, job numbers, phone numbers and dates.
   */
  public TableColumn<T, String> addFiguresColumn(
      String title, Function<T, String> value, ColumnWidth width) {
    TableColumn<T, String> column = addTextColumn(title, value, width);
    column.getStyleClass().add("figures");
    return column;
  }

  /**
   * A right-aligned amount column (header included), sorted by value and shown as {@code 1,234.00}
   * in tabular figures so the digits line up.
   */
  public TableColumn<T, Money> addMoneyColumn(String title, Function<T, Money> value) {
    TableColumn<T, Money> column = new TableColumn<>(title);
    column.getStyleClass().add("numeric");
    column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(value.apply(c.getValue())));
    column.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));
    column.setCellFactory(
        c ->
            new TableCell<>() {
              {
                getStyleClass().addAll("numeric", "text-numeric");
              }

              @Override
              protected void updateItem(Money money, boolean empty) {
                super.updateItem(money, empty);
                setText(empty || money == null ? null : money.formatPlain());
              }
            });
    ColumnWidth.range(140, 180).applyTo(column);
    table.getColumns().add(column);
    return column;
  }

  /** A column showing a node per row, such as a {@link StatusBadge}; sorted by {@code sortKey}. */
  public <K extends Comparable<K>> TableColumn<T, T> addNodeColumn(
      String title, Function<T, Node> node, Function<T, K> sortKey, ColumnWidth width) {
    TableColumn<T, T> column = new TableColumn<>(title);
    column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    column.setComparator(
        Comparator.comparing(sortKey, Comparator.nullsLast(Comparator.naturalOrder())));
    column.setCellFactory(
        c ->
            new TableCell<>() {
              @Override
              protected void updateItem(T row, boolean empty) {
                super.updateItem(row, empty);
                setText(null);
                setGraphic(empty || row == null ? null : node.apply(row));
              }
            });
    width.applyTo(column);
    table.getColumns().add(column);
    return column;
  }

  // ------------------------------------------------------------------ data and state

  /** Replaces the rows and leaves the loading or error state. */
  public void setItems(Collection<? extends T> rows) {
    state = State.READY;
    items.setAll(rows);
    refresh();
  }

  public ObservableList<T> getItems() {
    return items;
  }

  /** The rows currently shown, after search and sort. */
  public ObservableList<T> getVisibleItems() {
    return sorted;
  }

  /** Shows a loading message in place of the rows. */
  public void setLoading(boolean loading) {
    state = loading ? State.LOADING : State.READY;
    refresh();
  }

  /** Shows {@code message} in place of the rows, with a Retry button when {@code retry} is set. */
  public void setError(String message, Runnable retry) {
    state = State.ERROR;
    placeholder.showError(message, retry);
    refresh();
    count.setText("");
  }

  public T selected() {
    return table.getSelectionModel().getSelectedItem();
  }

  public void focusSearch() {
    search.requestFocus();
    search.selectAll();
  }

  public void clearSearch() {
    search.clear();
  }

  public TextField searchField() {
    return search;
  }

  public TableView<T> tableView() {
    return table;
  }

  // ------------------------------------------------------------------ internals

  private void applyFilter() {
    String query = query();
    filtered.setPredicate(query.isEmpty() ? null : row -> matcher.test(row, query));
    refresh();
  }

  private String query() {
    return search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
  }

  private void refresh() {
    // Searching an empty list is pointless, so the search box and count only appear with data.
    boolean hasData = state != State.READY || !items.isEmpty();
    search.setVisible(hasData);
    search.setManaged(hasData);
    search.setDisable(state != State.READY);
    count.setVisible(hasData && showCount);
    count.setManaged(hasData && showCount);
    switch (state) {
      case LOADING -> {
        table.setItems(none);
        placeholder.showLoading();
        count.setText("");
      }
      case ERROR -> table.setItems(none);
      case READY -> {
        table.setItems(sorted);
        if (items.isEmpty()) {
          placeholder.showMessage(emptyTitle, emptyText, emptyAction);
        } else if (filtered.isEmpty()) {
          placeholder.showNoMatches(search.getText().trim(), this::clearSearch);
        }
        count.setText(countText());
      }
    }
  }

  private String countText() {
    int total = items.size();
    int shown = filtered.size();
    String noun = total == 1 ? singular : plural;
    return shown == total ? total + " " + noun : shown + " of " + total + " " + noun;
  }

  private TableRow<T> newRow() {
    TableRow<T> row = new TableRow<>();
    row.setOnMouseClicked(
        e -> {
          if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty()) {
            onOpen.accept(row.getItem());
          }
        });
    return row;
  }
}
