package com.uzima.ui.dev;

import com.uzima.ui.component.ConfirmDialog;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.FormField;
import com.uzima.ui.component.LoadingOverlay;
import com.uzima.ui.component.StatePane;
import com.uzima.ui.component.Toast;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Dialogs, confirmations, toasts, the loading overlay and the state pane's states. */
final class OverlaysSection implements StyleGuideSection {

  @Override
  public String title() {
    return "Dialogs, toasts and states";
  }

  @Override
  public String description() {
    return "Dialogs open inside the window over a scrim; Esc cancels, Enter confirms, Tab stays"
        + " inside. Toasts confirm what happened. Every screen shows loading, empty and error"
        + " states the same way.";
  }

  @Override
  public Node build() {
    HBox dialogs = new HBox(24, onScrim(recordPayment()), onScrim(cancelJob()));
    HBox toasts =
        new HBox(
            16,
            new Toast(
                Toast.Kind.SUCCESS,
                "Payment recorded",
                "RCP-2026-00001 · KSh 84,400.00 · JOB-2026-00001"),
            new Toast(Toast.Kind.INFO, "Prices updated", "New quotes use today's catalog."),
            new Toast(Toast.Kind.ERROR, "Could not save", "The database is not reachable."));
    HBox states = new HBox(24, loadingCard(), emptyState(), errorState());
    for (Node n : states.getChildren()) {
      HBox.setHgrow(n, Priority.ALWAYS);
    }
    return new VBox(
        24,
        ButtonsSection.row("Dialog", dialogs),
        ButtonsSection.row("Toasts", toasts),
        ButtonsSection.row("States", states));
  }

  private static DialogShell recordPayment() {
    TextField amount = new TextField("600,000.00");
    ComboBox<String> method = new ComboBox<>();
    method.getItems().addAll("M-Pesa", "Cash", "Bank transfer", "Cheque");
    method.getSelectionModel().select(0);
    method.setMaxWidth(Double.MAX_VALUE);
    TextField reference = new TextField("SIN7K2QX4P");
    FormField referenceField = new FormField("M-Pesa reference", reference);
    referenceField.setRequired(true);
    referenceField.setError("This M-Pesa code is already on receipt RCP-2026-00001");
    VBox form =
        new VBox(
            new FormField("Amount (KSh)", amount), new FormField("Method", method), referenceField);
    form.getStyleClass().add("stack-16");
    DialogShell dialog = new DialogShell("Record payment");
    dialog.setSubtitle("JOB-2026-00001 · Athi River Cement Ltd · Balance KSh 600,000.00");
    dialog.setContent(form);
    dialog.setPrimary("Record payment", false);
    return dialog;
  }

  private static DialogShell cancelJob() {
    return new ConfirmDialog(
            "Cancel JOB-2026-00004?",
            "St. Mary's Girls School will owe nothing. The KSh 200,000.00 already received will show as a refund due.")
        .confirmLabel("Cancel job", true)
        .cancelLabel("Keep job")
        .requireReason("Reason for cancelling")
        .shell();
  }

  private static StackPane onScrim(DialogShell dialog) {
    StackPane scrim = new StackPane(dialog);
    scrim.getStyleClass().add("dialog-scrim");
    dialog.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
    dialog.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
    return scrim;
  }

  private static Node loadingCard() {
    Label title = new Label("Open jobs");
    title.getStyleClass().add("text-subtitle");
    Label value = new Label("4");
    value.getStyleClass().add("text-display");
    VBox card = new VBox(8, title, value, new Label("2 awaiting survey"));
    card.getStyleClass().add("card");
    card.setPrefSize(320, 240);
    LoadingOverlay overlay = new LoadingOverlay();
    StackPane stack = overlay.wrap(card);
    overlay.setShowDelay(Duration.ZERO);
    overlay.show("Loading…");
    return stack;
  }

  private static Node emptyState() {
    StatePane pane = new StatePane(new Label());
    pane.showEmpty("No clients yet", "Clients you register appear here.", "Add client", () -> {});
    return framed(pane);
  }

  private static Node errorState() {
    StatePane pane = new StatePane(new Label());
    pane.showError("The database is not reachable. Check the connection and try again.", () -> {});
    return framed(pane);
  }

  private static Node framed(StatePane pane) {
    VBox card = new VBox(pane);
    card.getStyleClass().add("card");
    card.setPrefSize(320, 240);
    return card;
  }
}
