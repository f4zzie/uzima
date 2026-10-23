package com.uzima.ui.component;

import static com.uzima.ui.component.MoneyFieldTest.robotFx;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.common.FieldError;
import com.uzima.domain.common.ValidationException;
import com.uzima.ui.UiTheme;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
class FieldErrorsTest {

  private FormField name;
  private FormField phone;
  private FormField email;

  @Start
  void start(Stage stage) {
    name = field("name", "Name");
    phone = field("phone", "Phone");
    email = field("email", "Email");
    stage.setScene(UiTheme.apply(new Scene(new VBox(name, phone, email), 400, 300)));
    stage.show();
    stage.toFront();
    stage.requestFocus();
  }

  @Test
  void errorsGoUnderTheirFieldsAndTheFirstInvalidFieldGetsFocus() {
    ValidationException e =
        new ValidationException(
            List.of(
                new FieldError("email", "Enter a full email address"),
                new FieldError("phone", "Enter a Kenyan phone number"),
                new FieldError("phone", "A second phone message is ignored")));
    AtomicReference<List<String>> unmatched = new AtomicReference<>();
    robotFx(() -> unmatched.set(FieldErrors.show(e, name, phone, email)));

    assertThat(unmatched.get()).isEmpty();
    assertThat(name.hasError()).isFalse();
    assertThat(phone.getError()).isEqualTo("Enter a Kenyan phone number");
    assertThat(email.getError()).isEqualTo("Enter a full email address");
    // Form order, not error order: phone comes before email on the form.
    assertThat(phone.getInput().isFocused()).isTrue();
  }

  @Test
  void messagesForOtherFieldsAreReturnedForABanner() {
    AtomicReference<List<String>> unmatched = new AtomicReference<>();
    robotFx(
        () ->
            unmatched.set(
                FieldErrors.show(
                    new ValidationException("status", "This job was already approved"),
                    name,
                    phone)));
    assertThat(unmatched.get()).containsExactly("This job was already approved");
  }

  @Test
  void showingAgainClearsEarlierErrors() {
    robotFx(() -> FieldErrors.show(new ValidationException("name", "Name is required"), name));
    robotFx(() -> FieldErrors.show(new ValidationException("phone", "Bad phone"), name, phone));
    assertThat(name.hasError()).isFalse();
    assertThat(phone.hasError()).isTrue();
    robotFx(() -> FieldErrors.clear(name, phone));
    assertThat(phone.hasError()).isFalse();
  }

  private static FormField field(String fieldName, String label) {
    FormField f = new FormField(label, new TextField());
    f.setName(fieldName);
    return f;
  }
}
