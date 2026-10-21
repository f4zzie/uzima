package com.uzima.ui.quote;

import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.MetreCharge;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteLine;
import com.uzima.domain.money.Money;
import java.util.List;
import java.util.Optional;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The live price breakdown beside the quote form: each service with its lines, the per-metre
 * working ("100 m × 1,000 + 50 m × 1,500"), fees, subtotal, VAT and the grand total. Everything
 * shown comes from the {@link Quote} the pricing engine returned; nothing is added up here.
 */
final class BreakdownPanel extends VBox {

  private static final List<LineCategory> SERVICES =
      List.of(LineCategory.DRILLING, LineCategory.PUMP, LineCategory.TANK, LineCategory.PLUMBING);

  private final VBox body = new VBox();
  private final QuoteEditorViewModel model;

  BreakdownPanel(QuoteEditorViewModel model) {
    this.model = model;
    getStyleClass().add("breakdown");
    Label title = new Label("Price breakdown");
    title.getStyleClass().add("text-subtitle");
    Label subtitle = new Label();
    subtitle.getStyleClass().add("text-caption");
    subtitle
        .textProperty()
        .bind(
            model
                .clientProperty()
                .map(c -> c.category().label() + " client · fees and VAT included")
                .orElse("Updates as you type"));
    body.getStyleClass().add("breakdown-body");
    getChildren().addAll(new VBox(2, title, subtitle), body);
    setFillWidth(true);
    model.quoteProperty().addListener((obs, old, now) -> render());
    model.missing().addListener((ListChangeListener<String>) c -> render());
    render();
  }

  private void render() {
    Quote quote = model.quoteProperty().get();
    body.getChildren().setAll(quote == null ? todo() : lines(quote));
  }

  /** What the user still has to enter before there is a price. */
  private Node todo() {
    VBox box = new VBox();
    box.getStyleClass().add("breakdown-todo");
    Label heading =
        new Label(model.missing().isEmpty() ? "Working out the price…" : "To see a price");
    heading.getStyleClass().add("text-strong");
    box.getChildren().add(heading);
    for (String item : model.missing()) {
      Label line = new Label("•  " + item);
      line.getStyleClass().add("text-muted");
      line.setWrapText(true);
      line.setMinHeight(Region.USE_PREF_SIZE);
      box.getChildren().add(line);
    }
    box.setAccessibleText(String.join(". ", model.missing()));
    return box;
  }

  private Node lines(Quote quote) {
    VBox box = new VBox();
    box.getStyleClass().add("breakdown-lines");
    for (LineCategory category : SERVICES) {
      if (!quote.totalFor(category).isZero()) {
        box.getChildren().add(service(quote, category));
      }
    }
    VBox fees = group("Fees", null);
    quote.lines().stream()
        .filter(
            l ->
                l.category() == LineCategory.SURVEY_FEE
                    || l.category() == LineCategory.AUTHORITY_FEE)
        .forEach(l -> fees.getChildren().add(row(l.description(), l.lineTotal(), null)));
    box.getChildren().addAll(fees, totals(quote));
    return box;
  }

  private Node service(Quote quote, LineCategory category) {
    VBox group = group(category.label(), quote.totalFor(category));
    for (QuoteLine line : quote.fixedLines(category)) {
      group.getChildren().add(row(line.description(), line.lineTotal(), null));
    }
    if (category == LineCategory.PLUMBING) {
      quote.lines().stream()
          .filter(l -> l.category() == category && !quote.fixedLines(category).contains(l))
          .forEach(
              l ->
                  group
                      .getChildren()
                      .add(
                          row(
                              l.description(),
                              l.lineTotal(),
                              new com.uzima.domain.common.Metres(l.quantity())
                                  + " × "
                                  + l.unitPrice().formatCompact())));
    } else {
      Optional<MetreCharge> metres = quote.metreCharge(category);
      metres.ifPresent(
          m ->
              group
                  .getChildren()
                  .add(
                      row(
                          (category == LineCategory.DRILLING ? "Depth " : "Lift height ")
                              + m.metres(),
                          m.total(),
                          m.working())));
    }
    return group;
  }

  private static VBox group(String title, Money total) {
    Label heading = new Label(title.toUpperCase(java.util.Locale.ROOT));
    heading.getStyleClass().add("text-overline");
    HBox header = new HBox(heading, spacer());
    if (total != null) {
      Label amount = new Label(total.formatPlain());
      amount.getStyleClass().addAll("text-overline", "figures", "breakdown-group-total");
      header.getChildren().add(amount);
    }
    header.setAlignment(Pos.CENTER_LEFT);
    VBox group = new VBox(header);
    group.getStyleClass().add("breakdown-group");
    return group;
  }

  /** One line: label and amount, with the working underneath when there is one. */
  private static Node row(String label, Money amount, String working) {
    Label name = new Label(label);
    name.setWrapText(true);
    name.setMinHeight(Region.USE_PREF_SIZE);
    HBox.setHgrow(name, Priority.ALWAYS);
    name.setMaxWidth(Double.MAX_VALUE);
    Label value = new Label(amount.formatPlain());
    value.getStyleClass().addAll("text-numeric", "breakdown-amount");
    value.setMinWidth(Region.USE_PREF_SIZE);
    HBox top = new HBox(12, name, value);
    top.setAlignment(Pos.TOP_LEFT);
    VBox row = new VBox(top);
    row.getStyleClass().add("breakdown-row");
    if (working != null) {
      Label maths = new Label(working);
      maths.getStyleClass().addAll("breakdown-working", "figures");
      maths.setWrapText(true);
      maths.setMinHeight(Region.USE_PREF_SIZE);
      row.getChildren().add(maths);
    }
    row.setAccessibleText(
        label + ": KSh " + amount.formatPlain() + (working == null ? "" : ", " + working));
    return row;
  }

  private static Node totals(Quote quote) {
    VBox box = new VBox();
    box.getStyleClass().add("breakdown-totals");
    box.getChildren()
        .addAll(
            row("Subtotal", quote.subtotal(), null),
            row("VAT " + quote.taxRate().formatPercent(), quote.tax(), null));
    Label label = new Label("Total");
    label.getStyleClass().add("text-subtitle");
    Label currency = new Label("KSh");
    currency.getStyleClass().addAll("text-muted", "breakdown-currency");
    Label total = new Label(quote.grandTotal().formatPlain());
    total.getStyleClass().addAll("text-headline", "figures-strong", "breakdown-grand-total");
    total.setId("grandTotal");
    HBox grand = new HBox(8, label, spacer(), currency, total);
    grand.setAlignment(Pos.BASELINE_RIGHT);
    grand.getStyleClass().add("breakdown-grand");
    grand.setAccessibleText("Grand total KSh " + quote.grandTotal().formatPlain());
    box.getChildren().add(grand);
    return box;
  }

  private static Region spacer() {
    Region r = new Region();
    HBox.setHgrow(r, Priority.ALWAYS);
    return r;
  }
}
