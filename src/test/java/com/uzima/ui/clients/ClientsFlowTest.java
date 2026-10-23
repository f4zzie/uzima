package com.uzima.ui.clients;

import static com.uzima.ui.FlowTestSupport.waitFor;
import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.County;
import com.uzima.domain.user.Role;
import com.uzima.service.AuditAction;
import com.uzima.service.AuditQuery;
import com.uzima.service.ClientSearch;
import com.uzima.ui.AppFlow;
import com.uzima.ui.FxTest;
import com.uzima.ui.KeyboardStageExtension;
import com.uzima.ui.SampleData;
import com.uzima.ui.ShellHarness;
import com.uzima.ui.StageSnapshots;
import com.uzima.ui.UiTestBackend;
import com.uzima.ui.component.FormField;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** The clients screens through the real shell, services and an in-memory database. */
@ExtendWith({ApplicationExtension.class, KeyboardStageExtension.class})
class ClientsFlowTest {

  private SampleData data;
  private ClientsTestPages pages;
  private Stage stage;
  private AppFlow flow;

  @Start
  void start(Stage stage) {
    this.stage = stage;
    data = SampleData.seed();
    pages = new ClientsTestPages(data.ui.backend);
    flow =
        ShellHarness.show(
            stage,
            data.ui.backend,
            "clients",
            nav -> new ClientsPage(data.ui.backend, nav, pages.attach(nav)).root());
  }

  @AfterEach
  void close() {
    data.close();
  }

  @Test
  void ctrlNRegistersAClientWhoAppearsInTheList(FxRobot robot) {
    waitFor(robot, "Wanjiru Kamau");
    StageSnapshots.saveAtStandardSizes(stage, "clients-list");
    robot.press(KeyCode.CONTROL, KeyCode.N).release(KeyCode.N, KeyCode.CONTROL);
    waitFor(robot, "#name");
    StageSnapshots.saveAtStandardSizes(stage, "client-new-dialog");

    robot.clickOn("#name").write("Njeri Wambui");
    choose(robot, "#category", ClientCategory.DOMESTIC);
    robot.clickOn("#phone").write("0711 222 333");
    robot.clickOn("#email").write("njeri.wambui@gmail.com");
    choose(robot, "#county", County.MURANGA);
    robot.clickOn("Register client");

    waitFor(robot, "UZM-DOM-0004");
    assertThat(robot.lookup("Client registered").tryQuery()).isPresent();
    assertThat(data.services.clients().search(ClientSearch.all()))
        .anySatisfy(
            c -> {
              assertThat(c.name()).isEqualTo("Njeri Wambui");
              assertThat(c.phone().value()).isEqualTo("+254711222333");
            });
  }

  @Test
  void aBadPhoneIsShownUnderThePhoneFieldWithoutABanner(FxRobot robot) {
    openNewClientDialog(robot);
    robot.clickOn("#name").write("Njeri Wambui");
    choose(robot, "#category", ClientCategory.DOMESTIC);
    robot.clickOn("#phone").write("0712");
    choose(robot, "#county", County.NAIROBI);
    robot.clickOn("Register client");

    FormField phone = field(robot, "#phone");
    waitUntil(phone::hasError);
    assertThat(phone.getError()).isNotBlank();
    assertThat(robot.lookup(".dialog .banner").query().isVisible()).isFalse();
    assertThat(FxTest.onFx(() -> robot.lookup("#phone").query().isFocused())).isTrue();
    StageSnapshots.saveAtStandardSizes(stage, "client-new-errors");
  }

  @Test
  void coordinatesOutsideKenyaWarnButStillSave(FxRobot robot) {
    openNewClientDialog(robot);
    robot.clickOn("#name").write("Kagera Ranch");
    choose(robot, "#category", ClientCategory.COMMERCIAL);
    robot.clickOn("#phone").write("0733 444 555");
    choose(robot, "#county", County.MIGORI);
    robot.clickOn("#latitude").write("-1.3500");
    robot.clickOn("#longitude").write("31.4000");

    Label warning = waitFor(robot, "#gpsWarning");
    assertThat(warning.getText()).contains("outside Kenya");
    StageSnapshots.saveAtStandardSizes(stage, "client-gps-warning");

    robot.clickOn("Register client");
    waitFor(robot, "UZM-COM-0003");
  }

  @Test
  void keyboardOnlyRegistration(FxRobot robot) {
    waitFor(robot, "Wanjiru Kamau");
    robot.press(KeyCode.CONTROL, KeyCode.N).release(KeyCode.N, KeyCode.CONTROL);
    FxTest.waitForFocus(stage, "#name");
    robot.write("Achieng Atieno");
    robot.type(KeyCode.TAB).write("d"); // category type-ahead: Domestic
    robot.type(KeyCode.TAB).write("0722 111 000");
    robot.type(KeyCode.TAB).type(KeyCode.TAB).write("ki"); // skip email; county Kiambu
    robot.type(KeyCode.TAB).write("Thika Road");
    robot.type(KeyCode.TAB).type(KeyCode.ENTER); // from latitude: Enter saves

    waitFor(robot, "UZM-DOM-0004");
    assertThat(data.services.clients().search(ClientSearch.all()))
        .anySatisfy(
            c -> {
              assertThat(c.name()).isEqualTo("Achieng Atieno");
              assertThat(c.category()).isEqualTo(ClientCategory.DOMESTIC);
              assertThat(c.county()).isEqualTo(County.KIAMBU);
            });
  }

  @Test
  void enterOpensTheClientDetailWithTheirJobs(FxRobot robot) {
    waitFor(robot, "Athi River Cement Ltd");
    robot.clickOn("Athi River Cement Ltd").type(KeyCode.ENTER);
    waitUntil(() -> "Athi River Cement Ltd".equals(topBarTitle(robot)));
    waitFor(robot, "JOB-2026-00001");
    assertThat(pages.calls).contains("client " + data.athiRiver.id());
    // Focus lands in the page (on its primary action), not back in the sidebar.
    FxTest.waitForFocus(stage, "#newJob");

    robot.clickOn("JOB-2026-00001").type(KeyCode.ENTER);
    assertThat(pages.calls).contains("job " + data.athiRiverJob.id());
  }

  @Test
  void theDetailShowsContactsTotalsAndJobs(FxRobot robot) {
    FxTest.onFx(() -> pages.client(data.athiRiver.id()));
    waitUntil(() -> "Athi River Cement Ltd".equals(topBarTitle(robot)));
    waitFor(robot, "JOB-2026-00001");
    assertThat(robot.lookup("KSh 600,000.00").tryQuery()).isPresent();
    assertThat(robot.lookup("Partially paid").tryQuery()).isPresent();
    StageSnapshots.saveAtStandardSizes(stage, "client-detail");

    robot.clickOn("#newJob");
    assertThat(pages.calls).contains("newJob " + data.athiRiver.id());
  }

  @Test
  void aCancelledJobShowsAsARefundDueOnTheClient(FxRobot robot) {
    FxTest.onFx(() -> pages.client(data.stMarys.id()));
    waitUntil(() -> "St. Mary's Girls School".equals(topBarTitle(robot)));
    waitFor(robot, "REFUND DUE");
    assertThat(robot.lookup("KSh 200,000.00").tryQuery()).isPresent();
    assertThat(robot.lookup("Refund due").tryQuery()).isPresent();
    assertThat(robot.lookup("Partially paid").tryQuery()).isEmpty();
    StageSnapshots.saveAtStandardSizes(stage, "client-detail-cancelled");
  }

  @Test
  void aManagerChangesTheCategoryAndItIsAudited(FxRobot robot) {
    signInAs(Role.MANAGER);
    FxTest.onFx(() -> pages.client(data.wanjiru.id()));
    waitUntil(() -> "Wanjiru Kamau".equals(topBarTitle(robot)));
    robot.clickOn("#editClient");
    waitFor(robot, "#changeCategory");
    StageSnapshots.saveAtStandardSizes(stage, "client-edit-dialog");

    robot.clickOn("#changeCategory");
    waitFor(robot, "#newCategory");
    choose(robot, "#newCategory", ClientCategory.COMMERCIAL);
    StageSnapshots.saveAtStandardSizes(stage, "client-category-change");
    robot.clickOn(robot.lookup(".dialog .button.primary").nth(1).queryAs(Node.class));

    waitUntil(
        () ->
            data.services.clients().find(data.wanjiru.id()).orElseThrow().category()
                == ClientCategory.COMMERCIAL);
    assertThat(data.services.clients().find(data.wanjiru.id()).orElseThrow().code().value())
        .isEqualTo("UZM-DOM-0001");
    // Only administrators may read the audit log.
    data.services
        .auth()
        .login(UiTestBackend.ADMIN_USERNAME, UiTestBackend.ADMIN_PASSWORD.toCharArray());
    assertThat(data.services.audit().search(AuditQuery.latest(20)))
        .anySatisfy(
            e -> {
              assertThat(e.action()).isEqualTo(AuditAction.CLIENT_CATEGORY_CHANGED);
              assertThat(e.details()).contains("Domestic → Commercial");
            });
  }

  @Test
  void aClerkCanEditButNotChangeTheCategory(FxRobot robot) {
    signInAs(Role.CLERK);
    FxTest.onFx(() -> pages.client(data.wanjiru.id()));
    waitUntil(() -> "Wanjiru Kamau".equals(topBarTitle(robot)));
    assertThat(robot.lookup("#deactivateClient").tryQuery()).isEmpty();
    robot.clickOn("#editClient");
    waitFor(robot, "#categoryValue");
    assertThat(robot.lookup("#changeCategory").tryQuery()).isEmpty();
  }

  @Test
  void anEmptyDatabaseInvitesTheFirstClient(FxRobot robot) {
    try (var empty = new com.uzima.ui.UiTestBackend().signedInAsAdministrator()) {
      FxTest.onFx(
          () ->
              ShellHarness.show(
                  stage,
                  empty.backend,
                  "clients",
                  nav ->
                      new ClientsPage(empty.backend, nav, new ClientsTestPages(empty.backend))
                          .root()));
      waitFor(robot, "No clients yet");
      assertThat(robot.lookup("Register a client").tryQuery()).isPresent();
      StageSnapshots.saveAtStandardSizes(stage, "clients-empty");
    }
  }

  private void openNewClientDialog(FxRobot robot) {
    waitFor(robot, "Wanjiru Kamau");
    robot.clickOn("#newClient");
    waitFor(robot, "#name");
  }

  private void signInAs(Role role) {
    String username = role.name().toLowerCase(java.util.Locale.ROOT);
    var created =
        data.services
            .users()
            .create(username, role == Role.CLERK ? "Otieno Juma" : "Grace Mutua", role);
    data.services.auth().login(username, created.temporaryPassword().toCharArray());
  }

  private static <T> void choose(FxRobot robot, String query, T value) {
    @SuppressWarnings("unchecked")
    ComboBox<T> box = robot.lookup(query).queryAs(ComboBox.class);
    FxTest.onFx(() -> box.setValue(value));
  }

  private static FormField field(FxRobot robot, String inputQuery) {
    TextInputControl input = robot.lookup(inputQuery).query();
    return FxTest.onFx(() -> (FormField) input.getParent());
  }

  private static String topBarTitle(FxRobot robot) {
    return FxTest.onFx(() -> robot.lookup(".top-bar-title").queryAs(Label.class).getText());
  }
}
