package com.uzima.ui.shell;

import com.uzima.ui.Views;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The signed-in frame: sidebar navigation, top bar with the account menu, and the content area.
 *
 * <p>Keyboard: Tab reaches the sidebar items, Up/Down move between them, and Ctrl+1…9 opens the
 * first nine pages from anywhere in the shell.
 */
public final class AppShell {

  private final ShellViewModel model;
  private final Parent root;
  private final ToggleGroup group = new ToggleGroup();

  @FXML private VBox nav;
  @FXML private Label shortcutHint;
  @FXML private Label title;
  @FXML private MenuButton userMenu;
  @FXML private Label initials;
  @FXML private Label userName;
  @FXML private Label userRole;
  @FXML private MenuItem lockItem;
  @FXML private SeparatorMenuItem lockSeparator;
  @FXML private StackPane content;

  public AppShell(ShellViewModel model) {
    this.model = model;
    this.root = Views.load("shell", this);
    root.addEventFilter(KeyEvent.KEY_PRESSED, this::onShortcut);
  }

  public Parent root() {
    return root;
  }

  @FXML
  private void initialize() {
    title.textProperty().bind(model.titleProperty());
    initials.setText(model.initials());
    userName.setText(model.displayName());
    userRole.setText(model.roleLabel());
    boolean canLock = model.lockAction().isPresent();
    lockItem.setVisible(canLock);
    lockSeparator.setVisible(canLock);

    buildNavigation(model.destinations());
    model.selectedProperty().addListener((obs, old, now) -> show(now));
    show(model.selectedProperty().get());
    // Keyboard users start in the navigation rather than on the account menu.
    Platform.runLater(
        () ->
            nav.getChildren().stream()
                .filter(n -> ((ToggleButton) n).isSelected())
                .findFirst()
                .ifPresent(Node::requestFocus));
  }

  private void buildNavigation(List<Destination> destinations) {
    for (int i = 0; i < destinations.size(); i++) {
      Destination d = destinations.get(i);
      ToggleButton item = new ToggleButton(d.title(), d.icon().node());
      item.getStyleClass().add("nav-item");
      item.setId("nav-" + d.id());
      item.setMaxWidth(Double.MAX_VALUE);
      item.setToggleGroup(group);
      item.setUserData(d);
      item.setAccessibleHelp(i < 9 ? "Ctrl+" + (i + 1) : null);
      item.setOnAction(e -> model.selectedProperty().set(d));
      item.addEventHandler(KeyEvent.KEY_PRESSED, this::onNavKey);
      nav.getChildren().add(item);
    }
    shortcutHint.setText(
        destinations.isEmpty()
            ? ""
            : "Ctrl+1–" + Math.min(9, destinations.size()) + " to switch pages");
    // Clicking the selected item must not leave nothing selected.
    group
        .selectedToggleProperty()
        .addListener(
            (obs, old, now) -> {
              if (now == null && old != null) {
                old.setSelected(true);
              }
            });
  }

  private void show(Destination destination) {
    for (Node n : nav.getChildren()) {
      ToggleButton item = (ToggleButton) n;
      item.setSelected(item.getUserData() == destination);
    }
    Node page = destination == null ? WelcomePanel.build(model) : destination.view().get();
    content.getChildren().setAll(page);
  }

  private void onNavKey(KeyEvent e) {
    int index = nav.getChildren().indexOf((Node) e.getSource());
    int target =
        switch (e.getCode()) {
          case DOWN -> index + 1;
          case UP -> index - 1;
          case HOME -> 0;
          case END -> nav.getChildren().size() - 1;
          default -> -1;
        };
    if (target >= 0 && target < nav.getChildren().size()) {
      nav.getChildren().get(target).requestFocus();
      e.consume();
    }
  }

  private void onShortcut(KeyEvent e) {
    if (!e.isShortcutDown() || e.isAltDown()) {
      return;
    }
    KeyCode code = e.getCode();
    int digit = code.isDigitKey() ? code.getChar().charAt(0) - '1' : -1;
    if (digit >= 0 && digit < 9 && model.select(digit)) {
      nav.getChildren().get(digit).requestFocus();
      e.consume();
    }
  }

  @FXML
  private void lock() {
    model.lockAction().ifPresent(Runnable::run);
  }

  @FXML
  private void signOut() {
    userMenu.setDisable(true);
    model.signOut();
  }
}
