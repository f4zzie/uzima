package com.uzima.ui.quote;

import com.uzima.domain.common.DomainException;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.Quote;
import com.uzima.domain.money.Money;
import com.uzima.ui.Backend;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.OverlayHost;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Shows what a pending job would cost at today's prices, side by side with what it was quoted at,
 * and saves the new prices only if the user agrees. If prices move again before they confirm, the
 * service refuses and the dialog says so.
 */
final class RepriceDialog {

  private RepriceDialog() {}

  /** Asks the service for the comparison, then shows it (or says nothing would change). */
  static void open(QuoteEditorViewModel model, OverlayHost overlays) {
    long jobId = model.jobId().orElseThrow();
    Backend backend = model.backend();
    backend.run(
        s -> s.jobs().previewReprice(jobId),
        repricing -> {
          if (!repricing.changesAnything()) {
            overlays.info("Prices are current", "Today's prices give the same quote.");
            return;
          }
          overlays.showDialog(dialog(repricing, model, overlays));
        },
        error -> overlays.error("Couldn't check today's prices", error.getMessage()));
  }

  static DialogShell dialog(
      Job.Repricing repricing, QuoteEditorViewModel model, OverlayHost overlays) {
    Quote before = repricing.beforeQuote();
    Quote after = repricing.afterQuote();
    GridPane grid = new GridPane();
    grid.getStyleClass().add("reprice-grid");
    grid.addRow(0, head(""), head("As quoted"), head("Today"));
    int row = 1;
    for (LineCategory category : LineCategory.values()) {
      if (before.totalFor(category).isZero() && after.totalFor(category).isZero()) {
        continue;
      }
      grid.addRow(
          row++,
          text(category.label()),
          amount(before.totalFor(category)),
          amount(after.totalFor(category)));
    }
    grid.addRow(row++, text("Subtotal"), amount(before.subtotal()), amount(after.subtotal()));
    grid.addRow(
        row++,
        text("VAT"),
        amount(before.tax(), before.taxRate().formatPercent()),
        amount(after.tax(), after.taxRate().formatPercent()));
    Label total = text("Total");
    total.getStyleClass().add("text-strong");
    Label beforeTotal = amount(before.grandTotal());
    Label afterTotal = amount(after.grandTotal());
    beforeTotal.getStyleClass().add("figures-strong");
    afterTotal.getStyleClass().add("figures-strong");
    grid.addRow(row, total, beforeTotal, afterTotal);
    grid.getStyleClass().add("figures");
    ColumnConstraints first = new ColumnConstraints();
    first.setHgrow(Priority.ALWAYS);
    ColumnConstraints number = new ColumnConstraints();
    number.setHalignment(HPos.RIGHT);
    number.setMinWidth(140);
    ColumnConstraints number2 = new ColumnConstraints();
    number2.setHalignment(HPos.RIGHT);
    number2.setMinWidth(140);
    grid.getColumnConstraints().addAll(first, number, number2);

    Money change = repricing.grandTotalChange();
    Label summary =
        new Label(
            change.isNegative()
                ? "The job gets cheaper by KSh " + change.negate().formatPlain() + "."
                : "The job costs KSh " + change.formatPlain() + " more.");
    summary.setWrapText(true);
    Label note =
        new Label(
            "Re-pricing uses today's catalog, the client's current category fees and today's VAT"
                + " rate. It is recorded in the audit log.");
    note.getStyleClass().add("text-caption");
    note.setWrapText(true);

    DialogShell dialog = new DialogShell("Re-price at today's prices?", DialogShell.Size.MEDIUM);
    dialog.setSubtitle(repricing.before().number().value());
    dialog.setContent(new VBox(16, summary, grid, note));
    dialog.setPrimary("Use today's prices", false);
    dialog.setCancelLabel("Keep quoted prices");
    dialog.setOnPrimary(
        d -> {
          d.setBusy(true);
          long jobId = repricing.before().id();
          model
              .backend()
              .run(
                  s -> s.jobs().applyReprice(jobId, after.grandTotal()),
                  saved -> {
                    d.close(DialogShell.Outcome.CONFIRMED);
                    overlays.success(
                        "Quote re-priced",
                        saved.number().value()
                            + " · KSh "
                            + saved.quote().grandTotal().formatPlain());
                    model.load();
                  },
                  error -> {
                    d.setBusy(false);
                    d.showError(
                        error instanceof DomainException
                            ? error.getMessage()
                            : "The new prices couldn't be saved. Try again.");
                  });
        });
    return dialog;
  }

  private static Label head(String text) {
    Label l = new Label(text);
    l.getStyleClass().add("text-overline");
    return l;
  }

  private static Label text(String text) {
    return new Label(text);
  }

  private static Label amount(Money money) {
    Label l = new Label(money.formatPlain());
    l.getStyleClass().add("text-numeric");
    return l;
  }

  private static Label amount(Money money, String rate) {
    Label l = amount(money);
    l.setText(money.formatPlain() + " (" + rate + ")");
    return l;
  }
}
