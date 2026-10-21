package com.uzima.ui.shell;

import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.ui.FxTest;
import com.uzima.ui.UiTestBackend;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShellViewModelTest {

  private UiTestBackend ui;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    ui = new UiTestBackend().signedInAsAdministrator();
  }

  @AfterEach
  void tearDown() {
    ui.close();
  }

  private User admin() {
    return ui.services.session().requireUser();
  }

  private ShellViewModel model(User user, Runnable onLock, Runnable onSignedOut) {
    return FxTest.onFx(
        () -> new ShellViewModel(ui.backend, user, SampleDestinations.all(), onLock, onSignedOut));
  }

  @Test
  void destinationsAreFilteredByRole() {
    User clerk = ui.services.users().create("otieno", "Otieno Ochieng", Role.CLERK).user();
    List<String> ids =
        model(clerk, null, () -> {}).destinations().stream().map(Destination::id).toList();
    assertThat(ids).containsExactly("dashboard", "clients", "jobs", "payments");
    assertThat(model(admin(), null, () -> {}).destinations()).hasSize(9);
  }

  @Test
  void firstDestinationIsOpenAndTitleFollowsSelection() {
    ShellViewModel model = model(admin(), null, () -> {});
    assertThat(model.titleProperty().get()).isEqualTo("Dashboard");
    assertThat(FxTest.onFx(() -> model.select(2))).isTrue();
    assertThat(model.titleProperty().get()).isEqualTo("Jobs");
    assertThat(model.select(9)).isFalse();
    assertThat(model.select(-1)).isFalse();
  }

  @Test
  void withNoDestinationsTheTitleWelcomesTheUser() {
    ShellViewModel model =
        FxTest.onFx(() -> new ShellViewModel(ui.backend, admin(), List.of(), null, () -> {}));
    assertThat(model.titleProperty().get()).isEqualTo("Welcome, Amina");
    assertThat(model.selectedProperty().get()).isNull();
  }

  @Test
  void initialsAndLabels() {
    ShellViewModel model = model(admin(), null, () -> {});
    assertThat(model.initials()).isEqualTo("AO");
    assertThat(model.displayName()).isEqualTo("Amina Odhiambo");
    assertThat(model.roleLabel()).isEqualTo("Admin");
  }

  @Test
  void lockIsOnlyOfferedWhenTheAppProvidesIt() {
    assertThat(model(admin(), null, () -> {}).lockAction()).isEmpty();
    assertThat(model(admin(), () -> {}, () -> {}).lockAction()).isPresent();
  }

  @Test
  void signOutEndsTheSessionThenCallsBack() {
    AtomicBoolean signedOut = new AtomicBoolean();
    ShellViewModel model = model(admin(), null, () -> signedOut.set(true));
    FxTest.onFx(model::signOut);
    waitUntil(signedOut::get);
    assertThat(ui.services.session().currentUser()).isEmpty();
  }
}
