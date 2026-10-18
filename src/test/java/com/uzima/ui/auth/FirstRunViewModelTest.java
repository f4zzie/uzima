package com.uzima.ui.auth;

import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.ui.FxTest;
import com.uzima.ui.UiTestBackend;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FirstRunViewModelTest {

  private UiTestBackend ui;
  private final AtomicReference<User> created = new AtomicReference<>();
  private FirstRunViewModel model;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    ui = new UiTestBackend();
    model = FxTest.onFx(() -> new FirstRunViewModel(ui.backend, created::set));
  }

  @AfterEach
  void tearDown() {
    ui.close();
  }

  private void fill(String display, String username, String password, String confirm) {
    FxTest.onFx(
        () -> {
          model.displayNameProperty().set(display);
          model.usernameProperty().set(username);
          model.passwordProperty().set(password);
          model.confirmProperty().set(confirm);
          model.create();
        });
  }

  @Test
  void createsTheAdministrator() {
    fill("Amina Odhiambo", "amina", "borehole-water-2026", "borehole-water-2026");
    waitUntil(() -> created.get() != null);
    assertThat(created.get().role()).isEqualTo(Role.ADMIN);
    assertThat(ui.services.session().currentUser()).isPresent();
    assertThat(model.passwordProperty().get()).isEmpty();
  }

  @Test
  void serviceErrorsLandOnTheirFields() {
    fill("", "a!", "short", "short");
    waitUntil(() -> !model.busyProperty().get() && !model.usernameErrorProperty().get().isEmpty());
    assertThat(model.displayNameErrorProperty().get()).isEqualTo("Display name is required");
    assertThat(model.passwordErrorProperty().get()).contains("at least 10");
    assertThat(model.failureProperty().get()).isEmpty();
  }

  @Test
  void mismatchStopsBeforeTheService() {
    fill("Amina Odhiambo", "amina", "borehole-water-2026", "different-password");
    assertThat(model.confirmErrorProperty().get()).isEqualTo("Passwords don't match");
    assertThat(model.busyProperty().get()).isFalse();
    assertThat(ui.services.auth().needsAdministrator()).isTrue();
  }

  @Test
  void ruleViolationsGoToTheBanner() {
    ui.withAdministrator();
    fill("Second Admin", "second", "borehole-water-2026", "borehole-water-2026");
    waitUntil(() -> !model.failureProperty().get().isEmpty());
    assertThat(model.failureProperty().get()).contains("already exists");
    assertThat(created.get()).isNull();
  }
}
