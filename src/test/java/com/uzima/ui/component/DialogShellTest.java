package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import com.uzima.ui.KeyboardStageExtension;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;

@ExtendWith(KeyboardStageExtension.class)
class DialogShellTest {

  private final FxRobot robot = new FxRobot();
  private final AtomicInteger contentClicks = new AtomicInteger();
  private Button contentButton;
  private TextField contentField;
  private OverlayHost host;
  private Scene scene;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    FxTest.onFx(
        () -> {
          contentButton = new Button("Behind the dialog");
          contentButton.setOnAction(e -> contentClicks.incrementAndGet());
          contentField = new TextField();
          host = new OverlayHost(new VBox(contentButton, contentField));
        });
    scene = ComponentStage.show(host);
    FxTest.onFx(() -> contentButton.requestFocus());
  }

  @Test
  void escapeCancelsAndRestoresFocus() {
    DialogShell dialog = open(form());

    robot.press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
    FxTest.settle();

    assertThat(dialog.result().toCompletableFuture())
        .isCompletedWithValue(DialogShell.Outcome.CANCELLED);
    assertThat(host.isDialogOpen()).isFalse();
    assertThat(scene.getFocusOwner()).isSameAs(contentButton);
  }

  @Test
  void enterInATextFieldRunsThePrimaryAction() {
    DialogShell dialog = open(form());
    robot.write("Wanjiru").press(KeyCode.ENTER).release(KeyCode.ENTER);
    FxTest.settle();
    assertThat(dialog.result().toCompletableFuture())
        .isCompletedWithValue(DialogShell.Outcome.CONFIRMED);
  }

  @Test
  void enterInATextAreaAddsANewLine() {
    TextArea notes = new TextArea();
    DialogShell dialog = open(new VBox(notes));
    robot.write("line one").press(KeyCode.ENTER).release(KeyCode.ENTER);
    FxTest.settle();
    assertThat(dialog.isClosed()).isFalse();
    assertThat(notes.getText()).contains("\n");
  }

  @Test
  void initialFocusIsTheFirstInput() {
    VBox form = form();
    open(form);
    assertThat(scene.getFocusOwner()).isSameAs(form.getChildren().get(0));
  }

  @Test
  void initialFocusFallsBackToThePrimaryButton() {
    DialogShell dialog = open(new VBox(new javafx.scene.control.Label("No inputs here")));
    assertThat(scene.getFocusOwner()).isSameAs(dialog.primaryButton());
  }

  @Test
  void tabAndShiftTabStayInsideTheDialog() {
    DialogShell dialog = open(form());
    for (int i = 0; i < 12; i++) {
      robot.press(KeyCode.TAB).release(KeyCode.TAB);
      FxTest.settle();
      assertThat(isInside(scene.getFocusOwner(), dialog)).isTrue();
    }
    for (int i = 0; i < 12; i++) {
      robot.press(KeyCode.SHIFT, KeyCode.TAB).release(KeyCode.TAB, KeyCode.SHIFT);
      FxTest.settle();
      assertThat(isInside(scene.getFocusOwner(), dialog)).isTrue();
    }
  }

  @Test
  void contentUnderTheDialogCannotBeClickedOrTypedInto() {
    open(form());
    robot.clickOn(contentButton);
    FxTest.onFx(() -> contentField.requestFocus());
    robot.write("typed behind");
    FxTest.settle();

    assertThat(contentClicks).hasValue(0);
    assertThat(contentField.getText()).isEmpty();
  }

  @Test
  void busyDialogIgnoresEscapeAndDisablesActions() {
    DialogShell dialog = open(form());
    FxTest.onFx(() -> dialog.setBusy(true));
    robot.press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
    FxTest.settle();

    assertThat(dialog.isClosed()).isFalse();
    assertThat(dialog.primaryButton().isDisabled()).isTrue();
    assertThat(dialog.cancelButton().isDisabled()).isTrue();

    FxTest.onFx(() -> dialog.setBusy(false));
    assertThat(dialog.primaryButton().isDisabled()).isFalse();
  }

  @Test
  void primaryHandlerDecidesWhenToClose() {
    DialogShell dialog = new DialogShell("Record payment");
    dialog.setContent(form());
    AtomicInteger calls = new AtomicInteger();
    dialog.setOnPrimary(
        d -> {
          if (calls.incrementAndGet() == 1) {
            d.showError("M-Pesa reference is already used on RCP-2026-00004");
          } else {
            d.close(DialogShell.Outcome.CONFIRMED);
          }
        });
    FxTest.onFx(() -> host.showDialog(dialog));

    robot.clickOn(dialog.primaryButton());
    assertThat(dialog.isClosed()).isFalse();
    assertThat(dialog.banner().isShowing()).isTrue();

    robot.clickOn(dialog.primaryButton());
    assertThat(dialog.isClosed()).isTrue();
  }

  @Test
  void stackedDialogsCloseTopFirst() {
    DialogShell first = open(form());
    DialogShell second = new DialogShell("Discard changes?", DialogShell.Size.SMALL);
    FxTest.onFx(() -> host.showDialog(second));

    assertThat(host.topDialog()).isSameAs(second);
    robot.press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
    FxTest.settle();

    assertThat(second.isClosed()).isTrue();
    assertThat(first.isClosed()).isFalse();
    assertThat(host.topDialog()).isSameAs(first);
    assertThat(host.isDialogOpen()).isTrue();
    assertThat(isInside(scene.getFocusOwner(), first)).isTrue();
  }

  @Test
  void closeButtonAndCancelButtonBothCancel() {
    DialogShell dialog = open(form());
    robot.clickOn(dialog.closeButton());
    assertThat(dialog.result().toCompletableFuture())
        .isCompletedWithValue(DialogShell.Outcome.CANCELLED);

    DialogShell again = open(form());
    robot.clickOn(again.cancelButton());
    assertThat(again.isClosed()).isTrue();
  }

  @Test
  void closingTwiceIsHarmlessAndOnCloseRunsOnce() {
    AtomicInteger closes = new AtomicInteger();
    DialogShell dialog = new DialogShell("Edit client").setSubtitle("UZM-DOM-0007");
    dialog.setOnClose(o -> closes.incrementAndGet());
    FxTest.onFx(() -> host.showDialog(dialog));
    FxTest.onFx(
        () -> {
          dialog.close(DialogShell.Outcome.CONFIRMED);
          dialog.close(DialogShell.Outcome.CANCELLED);
        });
    assertThat(closes).hasValue(1);
    assertThat(dialog.result().toCompletableFuture())
        .isCompletedWithValue(DialogShell.Outcome.CONFIRMED);
  }

  @Test
  void aDialogTallerThanTheWindowScrollsInsteadOfOverflowing() {
    VBox tall = new VBox();
    for (int i = 0; i < 40; i++) {
      tall.getChildren().add(new TextField("Field " + i));
    }
    DialogShell dialog = open(tall);
    FxTest.onFx(() -> host.layout());
    assertThat(dialog.getHeight()).isLessThanOrEqualTo(scene.getHeight());
    assertThat(dialog.primaryButton().localToScene(0, 0).getY()).isLessThan(scene.getHeight());
  }

  @Test
  void hostIsFoundFromInsideTheContent() {
    assertThat(OverlayHost.of(contentButton)).isSameAs(host);
  }

  private DialogShell open(Node content) {
    DialogShell dialog = new DialogShell("Edit client");
    dialog.setContent(content);
    FxTest.onFx(() -> host.showDialog(dialog));
    FxTest.settle();
    return dialog;
  }

  private static VBox form() {
    return new VBox(new TextField(), new TextField());
  }

  private static boolean isInside(Node node, Node ancestor) {
    for (Node n = node; n != null; n = n.getParent()) {
      if (n == ancestor) {
        return true;
      }
    }
    return false;
  }
}
