package com.uzima.ui.shell;

import com.uzima.ui.Views;
import com.uzima.ui.component.OverlayHost;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
  private final Deque<Page> stack = new ArrayDeque<>();
  private final Navigator navigator = new ShellNavigator();

  /** A page in the content area and its top-bar title. */
  private record Page(String title, Node node) {}

  @FXML private VBox nav;
  @FXML private Label shortcutHint;
  @FXML private Button backButton;
  @FXML private Label trail;
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
    model.titleProperty().addListener((obs, old, now) -> updateTopBar());
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
    // Unless a page has already taken focus by the time this runs.
    Platform.runLater(
        () -> {
          if (!focusInsideContent()) {
            nav.getChildren().stream()
                .filter(n -> ((ToggleButton) n).isSelected())
                .findFirst()
                .ifPresent(Node::requestFocus);
          }
        });
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
      item.setOnAction(
          e -> {
            // Keep the current page selected until the page agrees to be left.
            show(model.selectedProperty().get(), false);
            leaveThen(() -> model.selectedProperty().set(d));
          });
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

  private boolean focusInsideContent() {
    Node owner = root.getScene() == null ? null : root.getScene().getFocusOwner();
    for (Node n = owner; n != null; n = n.getParent()) {
      if (n == content) {
        return true;
      }
    }
    return false;
  }

  /** The navigator pages use to open detail pages and other sidebar pages. */
  public Navigator navigator() {
    return navigator;
  }

  private void show(Destination destination) {
    show(destination, true);
  }

  private void show(Destination destination, boolean rebuild) {
    for (Node n : nav.getChildren()) {
      ToggleButton item = (ToggleButton) n;
      item.setSelected(item.getUserData() == destination);
    }
    if (!rebuild) {
      return;
    }
    stack.clear();
    Node page =
        destination == null ? WelcomePanel.build(model) : destination.view().apply(navigator);
    stack.push(new Page(model.titleProperty().get(), page));
    showTop();
  }

  private void showTop() {
    content.getChildren().setAll(stack.peek().node());
    updateTopBar();
  }

  /** Shows the page below the current one again, reloading it if it is {@link Refreshable}. */
  private void returnToTop() {
    showTop();
    Refreshable.of(stack.peek().node()).ifPresent(Refreshable::refresh);
  }

  private void updateTopBar() {
    boolean detail = stack.size() > 1;
    backButton.setVisible(detail);
    backButton.setManaged(detail);
    if (detail) {
      List<Page> pages = new ArrayList<>(stack);
      java.util.Collections.reverse(pages);
      trail.setText(
          String.join(
                  "  ›  ", pages.subList(0, pages.size() - 1).stream().map(Page::title).toList())
              + "  ›");
      title.setText(stack.peek().title());
    } else {
      trail.setText("");
      title.setText(model.titleProperty().get());
    }
    trail.setVisible(detail);
    trail.setManaged(detail);
  }

  @FXML
  private void back() {
    navigator.back();
  }

  /** Runs {@code leave} now, or once the current page's {@link LeaveGuard} agrees. */
  private void leaveThen(Runnable leave) {
    Page top = stack.peek();
    if (top == null || LeaveGuard.of(top.node()).map(g -> g.allowLeave(leave)).orElse(true)) {
      leave.run();
    }
  }

  /** The shell's {@link Navigator}: a stack of pages on top of the selected sidebar page. */
  private final class ShellNavigator implements Navigator {

    @Override
    public boolean open(String destinationId) {
      List<Destination> all = model.destinations();
      for (int i = 0; i < all.size(); i++) {
        if (all.get(i).id().equals(destinationId)) {
          Destination target = all.get(i);
          int index = i;
          leaveThen(
              () -> {
                if (model.selectedProperty().get() == target) {
                  show(target);
                } else {
                  model.select(index);
                }
              });
          return true;
        }
      }
      return false;
    }

    @Override
    public void push(String pageTitle, Node page) {
      stack.push(new Page(pageTitle, page));
      showTop();
    }

    @Override
    public void replace(String pageTitle, Node page) {
      if (stack.size() > 1) {
        stack.pop();
      }
      push(pageTitle, page);
    }

    @Override
    public void back() {
      if (stack.size() > 1) {
        leaveThen(
            () -> {
              stack.pop();
              returnToTop();
            });
      }
    }

    @Override
    public OverlayHost overlays() {
      return OverlayHost.of(root);
    }
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
    if (e.isAltDown() && e.getCode() == KeyCode.LEFT && stack.size() > 1) {
      navigator.back();
      e.consume();
      return;
    }
    if (!e.isShortcutDown() || e.isAltDown()) {
      return;
    }
    KeyCode code = e.getCode();
    int digit = code.isDigitKey() ? code.getChar().charAt(0) - '1' : -1;
    if (digit >= 0 && digit < 9 && digit < model.destinations().size()) {
      e.consume();
      leaveThen(
          () -> {
            model.select(digit);
            nav.getChildren().get(digit).requestFocus();
          });
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
