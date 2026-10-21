package com.uzima.ui.quote;

import com.uzima.domain.catalog.PipeType;
import com.uzima.ui.component.FormField;
import com.uzima.ui.component.MetresField;
import com.uzima.ui.component.MoneyField;
import com.uzima.ui.component.TypeAhead;
import com.uzima.ui.quote.QuoteEditorViewModel.PlumbingMode;
import java.util.function.BiConsumer;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Plumbing inputs: either pipe runs (type, diameter, length; add and remove rows) plus outlets, or
 * one agreed amount with a description. Registers its fields with the page by error key, so
 * problems appear under the right row.
 */
final class PlumbingEditor extends VBox {

  private final QuoteEditorViewModel model;
  private final BiConsumer<String, FormField> register;
  private final VBox rows = new VBox();
  private final VBox itemised = new VBox();
  private final VBox manual = new VBox();

  PlumbingEditor(QuoteEditorViewModel model, BiConsumer<String, FormField> register) {
    this.model = model;
    this.register = register;
    getStyleClass().add("plumbing-editor");

    ToggleGroup group = new ToggleGroup();
    RadioButton byRuns = new RadioButton("Pipe runs and outlets");
    RadioButton byAmount = new RadioButton("One agreed amount");
    byRuns.setToggleGroup(group);
    byAmount.setToggleGroup(group);
    byRuns.setSelected(model.plumbingModeProperty().get() == PlumbingMode.ITEMISED);
    byAmount.setSelected(model.plumbingModeProperty().get() == PlumbingMode.MANUAL);
    group
        .selectedToggleProperty()
        .addListener(
            (obs, old, now) ->
                model
                    .plumbingModeProperty()
                    .set(now == byAmount ? PlumbingMode.MANUAL : PlumbingMode.ITEMISED));
    model
        .plumbingModeProperty()
        .addListener(
            (obs, old, now) -> (now == PlumbingMode.MANUAL ? byAmount : byRuns).setSelected(true));
    HBox modes = new HBox(24, byRuns, byAmount);
    modes.getStyleClass().add("plumbing-modes");

    buildItemised();
    buildManual();
    itemised.visibleProperty().bind(model.plumbingModeProperty().isEqualTo(PlumbingMode.ITEMISED));
    itemised.managedProperty().bind(itemised.visibleProperty());
    manual.visibleProperty().bind(model.plumbingModeProperty().isEqualTo(PlumbingMode.MANUAL));
    manual.managedProperty().bind(manual.visibleProperty());
    getChildren().addAll(modes, itemised, manual);
  }

  private void buildItemised() {
    rows.getStyleClass().add("pipe-runs");
    model
        .runs()
        .addListener(
            (ListChangeListener<PipeRunRow>)
                c -> {
                  boolean structural = false;
                  while (c.next()) {
                    structural |= c.wasAdded() || c.wasRemoved();
                  }
                  if (structural) {
                    renderRows();
                  }
                });
    Button add = new Button("Add pipe run");
    add.getStyleClass().addAll("ghost", "small");
    add.setId("addPipeRun");
    add.setOnAction(
        e -> {
          model.runs().add(new PipeRunRow());
          rows.getChildren().getLast().lookup(".combo-box").requestFocus();
        });
    TextField outlets = new TextField();
    outlets.textProperty().bindBidirectional(model.outletsProperty());
    outlets.setPrefColumnCount(4);
    outlets.setId("outlets");
    FormField outletsField = new FormField("Outlets (taps, troughs)", outlets);
    outletsField.setMaxWidth(200);
    register.accept("outlets", outletsField);
    FormField runsField = new FormField();
    runsField.setName("plumbingRuns");
    register.accept("plumbingRuns", runsField);
    runsField.setInput(add);
    runsField.setLabel("");
    itemised.getStyleClass().add("stack-12");
    itemised.getChildren().addAll(rows, runsField, outletsField);
    renderRows();
  }

  private void renderRows() {
    rows.getChildren().clear();
    for (int i = 0; i < model.runs().size(); i++) {
      rows.getChildren().add(row(i, model.runs().get(i)));
    }
  }

  private HBox row(int index, PipeRunRow run) {
    ComboBox<PipeType> pipe = new ComboBox<>(model.pipes());
    pipe.setConverter(
        JobEditorPage.labels(p -> p.name() + " · KSh " + p.costPerMetre().formatCompact() + "/m"));
    pipe.valueProperty().bindBidirectional(run.pipeProperty());
    pipe.setPromptText("Pipe type");
    TypeAhead.install(pipe, PipeType::name);
    pipe.setMaxWidth(Double.MAX_VALUE);
    TextField diameter = new TextField();
    diameter.textProperty().bindBidirectional(run.diameterProperty());
    diameter.setPromptText("25");
    diameter.setPrefColumnCount(4);
    diameter.getStyleClass().add("figures");
    MetresField length = new MetresField();
    length.valueProperty().bindBidirectional(run.lengthProperty());
    length.setPromptText("0.00");
    Button remove = new Button("Remove");
    remove.getStyleClass().addAll("ghost", "small");
    remove.setAccessibleText("Remove pipe run " + (index + 1));
    remove.setOnAction(e -> model.runs().remove(run));

    FormField pipeField = new FormField(index == 0 ? "Pipe" : "", pipe);
    FormField diameterField = new FormField(index == 0 ? "Diameter (mm)" : "", diameter);
    FormField lengthField = new FormField(index == 0 ? "Length" : "", length);
    pipe.setAccessibleText("Pipe type, run " + (index + 1));
    diameter.setAccessibleText("Diameter in millimetres, run " + (index + 1));
    length.textField().setAccessibleText("Length in metres, run " + (index + 1));
    register.accept("pipe." + index, pipeField);
    register.accept("diameter." + index, diameterField);
    register.accept("length." + index, lengthField);
    HBox.setHgrow(pipeField, Priority.ALWAYS);
    diameterField.setPrefWidth(120);
    lengthField.setPrefWidth(150);
    HBox row = new HBox(12, pipeField, diameterField, lengthField, remove);
    row.setAlignment(index == 0 ? Pos.BOTTOM_LEFT : Pos.TOP_LEFT);
    row.getStyleClass().add("pipe-run");
    return row;
  }

  private void buildManual() {
    MoneyField amount = new MoneyField();
    amount.valueProperty().bindBidirectional(model.manualAmountProperty());
    amount.setId("plumbingAmount");
    TextField description = new TextField();
    description.textProperty().bindBidirectional(model.manualDescriptionProperty());
    description.setPromptText("e.g. Plumbing to house and cattle trough");
    description.setId("plumbingDescription");
    FormField amountField = new FormField("Amount", amount);
    amountField.setMaxWidth(260);
    FormField descriptionField = new FormField("Description", description);
    register.accept("plumbingAmount", amountField);
    register.accept("plumbingDescription", descriptionField);
    manual.getStyleClass().add("stack-12");
    manual.getChildren().addAll(amountField, descriptionField);
  }
}
