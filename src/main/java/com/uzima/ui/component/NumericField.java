package com.uzima.ui.component;

import com.uzima.domain.common.ValidationException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A text field for a non-negative decimal with up to two decimal places and a unit label beside it.
 * Typing is limited to digits, commas and one dot; the text is parsed into a domain value as the
 * user types and tidied into the standard format when the field loses focus. Parsing is delegated
 * to the domain type, so the UI never does its own number arithmetic.
 *
 * @param <V> the domain value type, e.g. Money or Metres
 */
public abstract class NumericField<V> extends HBox {

  private static final Pattern ALLOWED = Pattern.compile("[0-9,]*(\\.[0-9]{0,2})?");

  private final TextField text = new TextField();
  private final Label adornment;
  private final ObjectProperty<V> value = new SimpleObjectProperty<>(this, "value");
  private final ReadOnlyStringWrapper problem = new ReadOnlyStringWrapper(this, "problem");
  private boolean updating;

  protected NumericField(String unit, boolean leading) {
    getStyleClass().add("input-group");
    adornment = new Label(unit);
    adornment.getStyleClass().addAll("input-adornment", leading ? "leading" : "trailing");
    adornment.setMinWidth(USE_PREF_SIZE);
    text.getStyleClass().addAll("numeric", leading ? "has-leading" : "has-trailing");
    text.setTextFormatter(new TextFormatter<>(onlyDecimals()));
    HBox.setHgrow(text, Priority.ALWAYS);
    text.setMaxWidth(Double.MAX_VALUE);
    if (leading) {
      getChildren().addAll(adornment, text);
    } else {
      getChildren().addAll(text, adornment);
    }

    text.textProperty().addListener((obs, old, now) -> parseText(now));
    text.focusedProperty()
        .addListener(
            (obs, was, focused) -> {
              if (!focused && value.get() != null) {
                setTextQuietly(format(value.get()));
              }
            });
    value.addListener(
        (obs, old, now) -> {
          if (!updating) {
            setTextQuietly(now == null ? "" : format(now));
            problem.set(now == null ? blankMessage() : check(now));
          }
        });
    problem.set(blankMessage());
  }

  /** Parses cleaned input (commas removed); throws ValidationException with a user message. */
  protected abstract V parse(String plain);

  /** The tidy form shown when the field isn't being edited, e.g. "1,234.50". */
  protected abstract String format(V v);

  /** Extra rule on a parsed value; returns a message, or null when acceptable. */
  protected String check(V v) {
    return null;
  }

  /** The parsed value, or null while the field is blank or invalid. */
  public ObjectProperty<V> valueProperty() {
    return value;
  }

  public V getValue() {
    return value.get();
  }

  public void setValue(V v) {
    value.set(v);
  }

  /** Why the current text has no value ("Enter an amount", ...); null when it's valid. */
  public ReadOnlyStringProperty problemProperty() {
    return problem.getReadOnlyProperty();
  }

  public String getProblem() {
    return problem.get();
  }

  public TextField textField() {
    return text;
  }

  public void setPromptText(String prompt) {
    text.setPromptText(prompt);
  }

  public void setEditable(boolean editable) {
    text.setEditable(editable);
  }

  /** Re-checks the current text, e.g. after a rule such as allow-zero changed. */
  protected final void revalidate() {
    parseText(text.getText());
  }

  @Override
  public void requestFocus() {
    text.requestFocus();
  }

  private void parseText(String raw) {
    if (updating) {
      return;
    }
    String plain = raw == null ? "" : raw.replace(",", "").trim();
    V parsed = null;
    String message = null;
    if (plain.isEmpty() || plain.equals(".")) {
      message = blankMessage();
    } else {
      try {
        parsed = parse(plain);
        message = check(parsed);
      } catch (ValidationException e) {
        message = e.getMessage();
      }
    }
    updating = true;
    try {
      value.set(message == null ? parsed : null);
      problem.set(message);
    } finally {
      updating = false;
    }
  }

  /** The message shown for a blank field. */
  protected abstract String blankMessage();

  private void setTextQuietly(String s) {
    updating = true;
    try {
      text.setText(s);
    } finally {
      updating = false;
    }
  }

  private static UnaryOperator<TextFormatter.Change> onlyDecimals() {
    return change -> ALLOWED.matcher(change.getControlNewText()).matches() ? change : null;
  }
}
