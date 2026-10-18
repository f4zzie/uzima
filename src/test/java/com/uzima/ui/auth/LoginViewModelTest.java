package com.uzima.ui.auth;

import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.service.LoginResult;
import com.uzima.ui.FxTest;
import com.uzima.ui.UiTestBackend;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginViewModelTest {

  private UiTestBackend ui;
  private final AtomicReference<LoginResult.Success> signedIn = new AtomicReference<>();
  private LoginViewModel model;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    ui = new UiTestBackend().withAdministrator();
    model = FxTest.onFx(() -> new LoginViewModel(ui.backend, signedIn::set, null));
  }

  @AfterEach
  void tearDown() {
    ui.close();
  }

  @Test
  void blankFieldsAreReportedPerFieldAndNothingIsSent() {
    FxTest.onFx(model::signIn);
    assertThat(model.usernameErrorProperty().get()).isEqualTo("Enter your username");
    assertThat(model.passwordErrorProperty().get()).isEqualTo("Enter your password");
    assertThat(model.busyProperty().get()).isFalse();
  }

  @Test
  void successHandsTheUserOnAndClearsThePassword() {
    FxTest.onFx(
        () -> {
          model.usernameProperty().set(UiTestBackend.ADMIN_USERNAME);
          model.passwordProperty().set(UiTestBackend.ADMIN_PASSWORD);
          model.signIn();
          assertThat(model.busyProperty().get()).isTrue();
        });
    waitUntil(() -> signedIn.get() != null);
    assertThat(signedIn.get().user().username().value()).isEqualTo("admin");
    assertThat(model.passwordProperty().get()).isEmpty();
    assertThat(model.busyProperty().get()).isFalse();
  }

  @Test
  void failureShowsTheGenericMessage() {
    FxTest.onFx(
        () -> {
          model.usernameProperty().set("nobody");
          model.passwordProperty().set("whatever-123");
          model.signIn();
        });
    waitUntil(() -> !model.failureProperty().get().isEmpty());
    assertThat(model.failureProperty().get()).isEqualTo(LoginResult.FAILURE_MESSAGE);
    assertThat(model.passwordProperty().get()).isEmpty();
    assertThat(signedIn.get()).isNull();
  }

  @Test
  void initialMessageIsShown() {
    LoginViewModel withMessage =
        FxTest.onFx(() -> new LoginViewModel(ui.backend, s -> {}, LoginResult.FAILURE_MESSAGE));
    assertThat(withMessage.failureProperty().get()).isEqualTo(LoginResult.FAILURE_MESSAGE);
  }
}
