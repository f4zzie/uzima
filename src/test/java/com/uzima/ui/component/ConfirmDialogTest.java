package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

class ConfirmDialogTest {

  private final FxRobot robot = new FxRobot();
  private OverlayHost host;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    host = FxTest.onFx(() -> new OverlayHost(new Label("Job detail")));
    ComponentStage.show(host);
  }

  @Test
  void aMissingReasonShowsInlineAndKeepsTheDialogOpen() {
    ConfirmDialog confirm = cancelJob();
    FxTest.onFx(() -> host.showDialog(confirm.shell()));

    robot.clickOn(confirm.shell().primaryButton());

    assertThat(confirm.shell().isClosed()).isFalse();
    assertThat(confirm.reasonField().getError()).isEqualTo(ConfirmDialog.REASON_REQUIRED);
    assertThat(confirm.reasonField().errorNode().isVisible()).isTrue();
  }

  @Test
  void typingClearsTheErrorAndConfirmPassesTheTrimmedReason() {
    AtomicReference<String> reason = new AtomicReference<>();
    ConfirmDialog confirm =
        cancelJob()
            .onConfirm(
                (shell, r) -> {
                  reason.set(r);
                  shell.close(DialogShell.Outcome.CONFIRMED);
                });
    FxTest.onFx(() -> host.showDialog(confirm.shell()));
    robot.clickOn(confirm.shell().primaryButton());

    FxTest.ensureStageReadyForInput((Stage) FxTest.onFx(() -> host.getScene().getWindow()));
    robot.clickOn(confirm.reasonField().getInput()).write("  Client moved to Nakuru  ");
    assertThat(confirm.reasonField().hasError()).isFalse();
    robot.clickOn(confirm.shell().primaryButton());

    assertThat(reason).hasValue("Client moved to Nakuru");
    assertThat(confirm.shell().result().toCompletableFuture())
        .isCompletedWithValue(DialogShell.Outcome.CONFIRMED);
  }

  @Test
  void withoutAReasonOrHandlerConfirmJustCloses() {
    ConfirmDialog confirm =
        new ConfirmDialog("Sign out?", "Unsaved changes will be lost.").cancelLabel("Stay");
    FxTest.onFx(() -> host.showDialog(confirm.shell()));

    assertThat(confirm.shell().cancelButton().getText()).isEqualTo("Stay");
    assertThat(confirm.reason()).isEmpty();
    robot.clickOn(confirm.shell().primaryButton());
    assertThat(confirm.shell().result().toCompletableFuture())
        .isCompletedWithValue(DialogShell.Outcome.CONFIRMED);
  }

  @Test
  void dangerConfirmationUsesTheDangerButton() {
    ConfirmDialog confirm = cancelJob();
    assertThat(confirm.shell().primaryButton().getStyleClass())
        .contains("danger")
        .doesNotContain("primary");
    assertThat(confirm.shell().primaryButton().getText()).isEqualTo("Cancel job");
  }

  private static ConfirmDialog cancelJob() {
    return FxTest.onFx(
        () ->
            new ConfirmDialog(
                    "Cancel JOB-2026-00012?",
                    "The client will owe nothing. Payments already received show as refund due.")
                .confirmLabel("Cancel job", true)
                .cancelLabel("Keep job")
                .requireReason("Reason for cancelling"));
  }
}
