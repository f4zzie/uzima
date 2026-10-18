package com.uzima.ui.clients;

import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.County;
import com.uzima.domain.money.Money;
import com.uzima.domain.user.Role;
import com.uzima.ui.FxTest;
import com.uzima.ui.SampleData;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The client view models against the real services and the sample data. */
class ClientViewModelsTest {

  private SampleData data;

  @BeforeEach
  void seed() {
    FxTest.startToolkit();
    data = SampleData.seed();
  }

  @AfterEach
  void close() {
    data.close();
  }

  @Test
  void theListHidesInactiveClientsUntilAskedAndReportsLoadState() {
    data.services.clients().deactivate(data.chebet.id());
    ClientsViewModel model = FxTest.onFx(() -> new ClientsViewModel(data.ui.backend));
    FxTest.onFx(model::load);
    waitUntil(() -> model.stateProperty().get() == ClientsViewModel.State.READY);
    assertThat(model.snapshot()).hasSize(5).noneMatch(c -> c.name().equals("Chebet Kiprono"));

    FxTest.onFx(() -> model.showInactiveProperty().set(true));
    waitUntil(() -> model.snapshot().size() == 6);
  }

  @Test
  void savingMapsEachServiceErrorToItsField() {
    ClientFormViewModel form = FxTest.onFx(() -> new ClientFormViewModel(data.ui.backend));
    FxTest.onFx(
        () -> {
          form.phoneProperty().set("12");
          form.emailProperty().set("not-an-email");
          form.latitudeProperty().set("north");
          form.longitudeProperty().set("36.8");
          form.save(c -> {});
        });
    waitUntil(() -> !form.busyProperty().get());
    assertThat(form.errorProperty("name").get()).isNotEmpty();
    assertThat(form.errorProperty("category").get()).isNotEmpty();
    assertThat(form.errorProperty("phone").get()).isNotEmpty();
    assertThat(form.errorProperty("email").get()).isNotEmpty();
    assertThat(form.errorProperty("county").get()).isNotEmpty();
    assertThat(form.errorProperty("latitude").get()).isNotEmpty();
    // Every error belongs to a field, so there is no form-level banner message.
    assertThat(form.failureProperty().get()).isEmpty();
  }

  @Test
  void thePhoneIsShownInLocalFormAndTheSavedFormIsExplained() {
    ClientFormViewModel form = FxTest.onFx(() -> new ClientFormViewModel(data.ui.backend));
    FxTest.onFx(
        () -> {
          form.phoneProperty().set("+254711222333");
          form.phoneEdited();
        });
    assertThat(form.phoneProperty().get()).isEqualTo("0711 222 333");
    assertThat(form.phoneHelpProperty().get()).isEqualTo("Saved as +254711222333");
  }

  @Test
  void aPointOutsideKenyaWarnsWithoutBlocking() {
    ClientFormViewModel form = FxTest.onFx(() -> new ClientFormViewModel(data.ui.backend));
    FxTest.onFx(
        () -> {
          form.latitudeProperty().set("-1.29");
          form.longitudeProperty().set("36.82");
        });
    assertThat(form.gpsWarningProperty().get()).isEmpty();
    FxTest.onFx(() -> form.longitudeProperty().set("31.40"));
    assertThat(form.gpsWarningProperty().get()).isEqualTo(ClientFormViewModel.OUTSIDE_KENYA);

    AtomicReference<Client> saved = new AtomicReference<>();
    FxTest.onFx(
        () -> {
          form.nameProperty().set("Kagera Ranch");
          form.categoryProperty().set(ClientCategory.COMMERCIAL);
          form.phoneProperty().set("0733444555");
          form.countyProperty().set(County.MIGORI);
          form.save(saved::set);
        });
    waitUntil(() -> saved.get() != null);
    assertThat(saved.get().details().hasLocationOutsideKenya()).isTrue();
  }

  @Test
  void editingKeepsTheCategoryAndIdWhileUpdatingDetails() {
    Client wanjiru = data.services.clients().find(data.wanjiru.id()).orElseThrow();
    ClientFormViewModel form = FxTest.onFx(() -> new ClientFormViewModel(data.ui.backend, wanjiru));
    assertThat(form.isEdit()).isTrue();
    assertThat(form.phoneProperty().get()).isEqualTo("0712 345 678");
    assertThat(form.latitudeProperty().get()).isEqualTo("-1.146");

    AtomicReference<Client> saved = new AtomicReference<>();
    FxTest.onFx(
        () -> {
          form.addressProperty().set("Kamiti Road, Ruiru, near the chief's camp");
          form.save(saved::set);
        });
    waitUntil(() -> saved.get() != null);
    assertThat(saved.get().code().value()).isEqualTo("UZM-DOM-0001");
    assertThat(saved.get().category()).isEqualTo(ClientCategory.DOMESTIC);
    assertThat(saved.get().details().address()).contains("chief's camp");
  }

  @Test
  void categoryChangeOffersTheOtherCategoriesAndNamesTheId() {
    Client wanjiru = data.services.clients().find(data.wanjiru.id()).orElseThrow();
    CategoryChangeViewModel model =
        FxTest.onFx(() -> new CategoryChangeViewModel(data.ui.backend, wanjiru));
    assertThat(model.choices())
        .containsExactly(ClientCategory.INDUSTRIAL, ClientCategory.COMMERCIAL);
    assertThat(model.auditNote()).contains("UZM-DOM-0001").contains("audit log");
    assertThat(CategoryChangeViewModel.allowed(data.ui.backend)).isTrue();

    FxTest.onFx(() -> model.targetProperty().set(null));
    FxTest.onFx(() -> model.change(c -> {}));
    assertThat(model.errorProperty().get()).isEqualTo("Choose the new category");

    AtomicReference<Client> changed = new AtomicReference<>();
    FxTest.onFx(
        () -> {
          model.targetProperty().set(ClientCategory.COMMERCIAL);
          model.change(changed::set);
        });
    waitUntil(() -> changed.get() != null);
    assertThat(changed.get().category()).isEqualTo(ClientCategory.COMMERCIAL);
  }

  @Test
  void aClerkMayNotChangeCategories() {
    var clerk = data.services.users().create("clerk", "Otieno Juma", Role.CLERK);
    data.services.auth().login("clerk", clerk.temporaryPassword().toCharArray());
    assertThat(CategoryChangeViewModel.allowed(data.ui.backend)).isFalse();
  }

  @Test
  void totalsExcludeCancelledJobsFromQuotedButShowTheirRefund() {
    ClientDetailViewModel model =
        FxTest.onFx(() -> new ClientDetailViewModel(data.ui.backend, data.stMarys.id()));
    FxTest.onFx(model::load);
    waitUntil(() -> model.stateProperty().get() == ClientDetailViewModel.State.READY);
    ClientTotals totals = model.detailProperty().get().totals();
    assertThat(totals.jobs()).isEqualTo(1);
    assertThat(totals.quoted()).isEqualTo(Money.ZERO);
    assertThat(totals.paid()).isEqualTo(Money.of(200_000));
    assertThat(totals.balance()).isEqualTo(Money.ZERO);
    assertThat(totals.refundDue()).isEqualTo(Money.of(200_000));
  }

  @Test
  void aMissingClientShowsAnErrorState() {
    ClientDetailViewModel model =
        FxTest.onFx(() -> new ClientDetailViewModel(data.ui.backend, 999_999));
    FxTest.onFx(model::load);
    waitUntil(() -> model.stateProperty().get() == ClientDetailViewModel.State.ERROR);
    assertThat(model.errorProperty().get()).isEqualTo("This client no longer exists.");
  }

  @Test
  void deactivatingAndReactivatingReloadsTheClient() {
    ClientDetailViewModel model =
        FxTest.onFx(() -> new ClientDetailViewModel(data.ui.backend, data.otieno.id()));
    AtomicReference<Client> done = new AtomicReference<>();
    FxTest.onFx(() -> model.setActive(false, done::set, e -> {}));
    waitUntil(() -> done.get() != null && !done.get().active());
    FxTest.onFx(() -> model.setActive(true, done::set, e -> {}));
    waitUntil(() -> done.get().active());
  }
}
