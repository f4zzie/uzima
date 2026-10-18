package com.uzima.ui.component;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A labelled input with help text and an inline error underneath. Setting an error marks the input
 * {@code :invalid}, replaces the help text and becomes the input's accessible help, so screen
 * readers announce it too.
 *
 * <p>{@code name} matches the field names in {@link com.uzima.domain.common.FieldError}, which is
 * how service validation errors find their way to the right field.
 */
@DefaultProperty("input")
public class FormField extends VBox {

  private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
  private static final String ACCESSORY = "form-field-accessory";

  private final Label label = new Label();
  private final Label requiredMark = new Label("*");
  private final Label help = new Label();
  private final Label error = new Label();
  private final ObjectProperty<Node> input = new SimpleObjectProperty<>(this, "input");
  private String name = "";
  private String helpText = "";

  public FormField() {
    getStyleClass().add("form-field");
    label.getStyleClass().add("form-field-label");
    requiredMark.getStyleClass().add("form-field-required");
    requiredMark.setVisible(false);
    requiredMark.setManaged(false);
    help.getStyleClass().add("form-field-help");
    help.setWrapText(true);
    error.getStyleClass().add("form-field-error");
    error.setWrapText(true);
    HBox heading = new HBox(4, label, requiredMark);
    getChildren().addAll(heading, help, error);
    showText(help, "");
    showText(error, "");
    input.addListener(
        (obs, old, now) -> {
          if (old != null) {
            getChildren().remove(old);
          }
          if (now != null) {
            getChildren().add(1, now);
            label.setLabelFor(now);
          }
        });
  }

  public FormField(String labelText, Node input) {
    this();
    setLabel(labelText);
    setInput(input);
  }

  public String getLabel() {
    return label.getText();
  }

  public void setLabel(String text) {
    label.setText(text);
    applyAccessibleText();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? "" : name;
  }

  public boolean isRequired() {
    return requiredMark.isVisible();
  }

  public void setRequired(boolean required) {
    requiredMark.setVisible(required);
    requiredMark.setManaged(required);
  }

  public String getHelp() {
    return helpText;
  }

  public void setHelp(String text) {
    helpText = text == null ? "" : text;
    showText(help, hasError() ? "" : helpText);
  }

  public Node getInput() {
    return input.get();
  }

  public void setInput(Node node) {
    input.set(node);
    applyAccessibleText();
  }

  public ObjectProperty<Node> inputProperty() {
    return input;
  }

  /** Shows {@code message} under the input and marks it invalid; null or blank clears it. */
  public void setError(String message) {
    boolean invalid = message != null && !message.isBlank();
    showText(error, invalid ? message : "");
    showText(help, invalid ? "" : helpText);
    Node node = input.get();
    if (node != null) {
      node.pseudoClassStateChanged(INVALID, invalid);
      if (node instanceof Control control) {
        control.setAccessibleHelp(invalid ? message : helpText);
      }
    }
  }

  public void clearError() {
    setError(null);
  }

  public String getError() {
    return error.getText();
  }

  public boolean hasError() {
    return !error.getText().isEmpty();
  }

  /**
   * A node shown directly under the input, above the help and error text, such as a {@link
   * PasswordStrengthHint}.
   */
  public void setAccessory(Node accessory) {
    getChildren().removeIf(n -> n.getProperties().containsKey(ACCESSORY));
    if (accessory != null) {
      accessory.getProperties().put(ACCESSORY, Boolean.TRUE);
      int at = input.get() == null ? 1 : getChildren().indexOf(input.get()) + 1;
      getChildren().add(at, accessory);
    }
  }

  /** The label node, for tests and for layouts that align labels. */
  public Label labelNode() {
    return label;
  }

  public Label errorNode() {
    return error;
  }

  private void applyAccessibleText() {
    if (input.get() instanceof Control control && label.getText() != null) {
      control.setAccessibleText(label.getText());
    }
  }

  private static void showText(Label target, String text) {
    boolean show = text != null && !text.isEmpty();
    target.setText(show ? text : "");
    target.setVisible(show);
    target.setManaged(show);
  }
}
