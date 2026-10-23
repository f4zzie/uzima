package com.uzima.ui.quote;

import static com.uzima.ui.FlowTestSupport.waitFor;
import static com.uzima.ui.FlowTestSupport.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.Job;
import com.uzima.domain.money.Money;
import com.uzima.service.JobSearch;
import com.uzima.ui.AppFlow;
import com.uzima.ui.FxTest;
import com.uzima.ui.KeyboardStageExtension;
import com.uzima.ui.SampleData;
import com.uzima.ui.ShellHarness;
import com.uzima.ui.StageSnapshots;
import com.uzima.ui.component.Banner;
import com.uzima.ui.component.FormField;
import com.uzima.ui.quote.QuoteEditorViewModel.PlumbingMode;
import java.util.OptionalLong;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/** The quote editor end to end: real services, real pricing, headless. */
@ExtendWith({ApplicationExtension.class, KeyboardStageExtension.class})
class QuoteEditorFlowTest {

  private SampleData data;
  private RecordingPages pages;
  private Stage stage;
  private AppFlow flow;
  private JobEditorPage page;

  @Start
  void start(Stage stage) {
    this.stage = stage;
    data = SampleData.seed();
    pages = new RecordingPages();
    flow =
        ShellHarness.show(
            stage, data.ui.backend, "jobs", nav -> new javafx.scene.control.Label("Jobs"));
  }

  @AfterEach
  void close() {
    try {
      data.close();
    } finally {
      FxTest.onFx(
          () -> {
            if (stage != null && stage.isShowing()) {
              stage.hide();
            }
          });
      FxTest.settle();
    }
  }

  /** Opens the editor as a detail page over Jobs, as the app does. */
  private QuoteEditorViewModel open(OptionalLong clientId, OptionalLong jobId) {
    FxTest.onFx(
        () -> {
          var nav = flow.navigator().orElseThrow();
          page = new JobEditorPage(data.ui.backend, nav, pages, clientId, jobId);
          page.model()
              .stateProperty()
              .addListener(
                  (obs, old, now) ->
                      FxTest.markFocus(stage.getScene(), "editor state " + old + " -> " + now));
          FxTest.markFocus(stage.getScene(), "navigator.push(editor) sent");
          nav.push(page.title(), page.root());
          FxTest.markFocus(stage.getScene(), "navigator.push(editor) returned");
        });
    QuoteEditorViewModel model = page.model();
    waitUntil(() -> model.stateProperty().get() == QuoteEditorViewModel.State.READY);
    return model;
  }

  @Test
  void acceptanceExample1ShowsTheTotalAndTheTierWorking(FxRobot robot) {
    QuoteEditorViewModel model = open(OptionalLong.of(data.athiRiver.id()), OptionalLong.empty());
    fillExample1(robot, model);

    waitUntil(() -> "684,400.00".equals(text(robot, "#grandTotal")));
    assertThat(robot.lookup("100 m × 1,000 + 50 m × 1,500").tryQuery()).isPresent();
    assertThat(robot.lookup("590,000.00").tryQuery()).isPresent();
    assertThat(robot.lookup("94,400.00").tryQuery()).isPresent();
    StageSnapshots.saveAtStandardSizes(stage, "quote-editor-example1");

    robot.press(KeyCode.CONTROL, KeyCode.S).release(KeyCode.S, KeyCode.CONTROL);
    waitUntil(() -> pages.calls.stream().anyMatch(c -> c.startsWith("job ")));
    Job saved =
        data.services.jobs().find(Long.parseLong(pages.calls.getLast().substring(4))).orElseThrow();
    assertThat(saved.number().value()).isEqualTo("JOB-2026-00007");
    assertThat(saved.quote().grandTotal()).isEqualTo(Money.of(684_400));
  }

  private void fillExample1(FxRobot robot, QuoteEditorViewModel model) {
    FxTest.onFx(() -> model.drillingTypeProperty().set(drilling(model, "Symmetric")));
    robot.clickOn("#depth .text-field").write("150");
    FxTest.onFx(
        () -> {
          model.pumpOnProperty().set(true);
          model
              .pumpTypeProperty()
              .set(
                  model.pumpTypes().stream()
                      .filter(p -> p.name().equals("Submersible electric"))
                      .findFirst()
                      .orElseThrow());
        });
    robot.clickOn("#lift .text-field").write("10");
    FxTest.onFx(
        () -> {
          model.tankOnProperty().set(true);
          model
              .tankProperty()
              .set(
                  model.tanks().stream()
                      .filter(t -> t.capacityLitres() == 5000)
                      .findFirst()
                      .orElseThrow());
          model.plumbingOnProperty().set(true);
          model.plumbingModeProperty().set(PlumbingMode.MANUAL);
        });
    typeInto(robot, "#plumbingAmount .text-field", "50000");
    typeInto(robot, "#plumbingDescription", "Plumbing to house and trough");
  }

  @Test
  void aQuoteCanBeEnteredWithTheKeyboardAlone(FxRobot robot) {
    open(OptionalLong.empty(), OptionalLong.empty());
    FxTest.ensureStageReadyForInput(stage, "#client");
    robot.write("Che"); // type-ahead: Chebet Kiprono
    robot.type(KeyCode.TAB); // the Drilling switch (already on)
    robot.type(KeyCode.TAB).write("s"); // drilling type: Symmetric
    robot.type(KeyCode.TAB).write("60"); // depth
    waitUntil(() -> !text(robot, "#grandTotal").isEmpty());
    robot.press(KeyCode.CONTROL, KeyCode.S).release(KeyCode.S, KeyCode.CONTROL);

    waitUntil(() -> pages.calls.stream().anyMatch(c -> c.startsWith("job ")));
    assertThat(data.services.jobs().search(JobSearch.all().withClient(data.chebet.id())))
        .hasSize(2);
  }

  @Test
  void problemsShowUnderTheirFieldsWithoutABanner(FxRobot robot) {
    QuoteEditorViewModel model = open(OptionalLong.of(data.wanjiru.id()), OptionalLong.empty());
    FxTest.onFx(() -> model.drillingTypeProperty().set(drilling(model, "Symmetric")));
    robot.clickOn("#depth .text-field").write("0");
    FxTest.onFx(() -> model.plumbingOnProperty().set(true));
    robot.press(KeyCode.CONTROL, KeyCode.S).release(KeyCode.S, KeyCode.CONTROL);

    FormField depth = field(robot, "depth");
    waitUntil(depth::hasError);
    assertThat(depth.getError()).isEqualTo("Enter more than 0 m");
    assertThat(field(robot, "plumbingRuns").getError()).isEqualTo("Add at least one pipe run");
    assertThat(robot.lookup(".banner").queryAll()).allMatch(b -> !((Banner) b).isShowing());
    assertThat(pages.calls).isEmpty();
    StageSnapshots.saveAtStandardSizes(stage, "quote-editor-errors");
  }

  @Test
  void editingKeepsQuotedPricesAndRepricingComparesThem(FxRobot robot) {
    Money quoted = data.otienoJob.quote().grandTotal();
    FxTest.onFx(() -> {});
    DrillingType symmetric =
        data.services.catalog().activeDrillingTypes().stream()
            .filter(d -> d.name().equals("Symmetric"))
            .findFirst()
            .orElseThrow();
    data.services.catalog().changeItem(symmetric.withPrice(Money.of(150_000)));

    QuoteEditorViewModel model = open(OptionalLong.empty(), OptionalLong.of(data.otienoJob.id()));
    waitUntil(() -> quoted.formatPlain().equals(text(robot, "#grandTotal")));
    assertThat(robot.lookup(".quote-notice").query().isVisible()).isTrue();
    StageSnapshots.saveAtStandardSizes(stage, "quote-editor-edit");

    robot.clickOn("#reprice");
    waitFor(robot, "Use today's prices");
    assertThat(robot.lookup("As quoted").tryQuery()).isPresent();
    assertThat(robot.lookup("The job costs KSh 23,200.00 more.").tryQuery()).isPresent();
    StageSnapshots.saveAtStandardSizes(stage, "quote-reprice-dialog");
    robot.clickOn("Use today's prices");
    waitUntil(
        () ->
            !quoted.equals(
                data.services.jobs().find(data.otienoJob.id()).orElseThrow().quote().grandTotal()));
  }

  @Test
  void leavingWithUnsavedChangesAsksFirst(FxRobot robot) {
    QuoteEditorViewModel model = open(OptionalLong.of(data.wanjiru.id()), OptionalLong.empty());
    robot.clickOn("#depth .text-field").write("40");
    waitUntil(() -> model.dirtyProperty().get());
    robot.clickOn(".top-bar-back");
    waitFor(robot, "Discard changes?");
    robot.clickOn("Keep editing");
    assertThat(text(robot, ".top-bar-title")).isEqualTo("New quote");
    robot.clickOn(".top-bar-back");
    robot.clickOn("Discard changes");
    waitUntil(() -> "Jobs".equals(text(robot, ".top-bar-title")));
  }

  @Test
  void snapshotsOfTheEmptyAndItemisedStates(FxRobot robot) {
    QuoteEditorViewModel model = open(OptionalLong.empty(), OptionalLong.empty());
    StageSnapshots.saveAtStandardSizes(stage, "quote-editor-empty");

    FxTest.onFx(
        () -> {
          model
              .clientProperty()
              .set(
                  model.clients().stream()
                      .filter(c -> c.id() == data.otieno.id())
                      .findFirst()
                      .orElseThrow());
          model.drillingTypeProperty().set(drilling(model, "Symmetric"));
          model.depthProperty().set(Metres.of(120));
          model.plumbingOnProperty().set(true);
          model.plumbingModeProperty().set(PlumbingMode.ITEMISED);
          model.runs().add(new PipeRunRow(pipe(model, "PVC"), 25, Metres.of(40)));
          model.runs().add(new PipeRunRow(pipe(model, "HDPE"), 32, Metres.of(15)));
          model.outletsProperty().set("3");
        });
    waitUntil(() -> !text(robot, "#grandTotal").isEmpty());
    StageSnapshots.saveAtStandardSizes(stage, "quote-editor-itemised-plumbing");
  }

  /** Focuses a field (scrolled out of view or not) and types into it, as a keyboard user would. */
  private void typeInto(FxRobot robot, String query, String text) {
    Node node = FxTest.onFx(() -> robot.lookup(query).query());
    FxTest.onFx(node::requestFocus);
    FxTest.waitForFocus(stage, node);
    robot.write(text);
  }

  private static DrillingType drilling(QuoteEditorViewModel model, String name) {
    return model.drillingTypes().stream()
        .filter(d -> d.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private static com.uzima.domain.catalog.PipeType pipe(QuoteEditorViewModel model, String name) {
    return model.pipes().stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
  }

  private FormField field(FxRobot robot, String name) {
    return FxTest.onFx(
        () ->
            robot.lookup(".form-field").queryAllAs(FormField.class).stream()
                .filter(f -> name.equals(f.getName()))
                .findFirst()
                .orElseThrow());
  }

  private static String text(FxRobot robot, String query) {
    return FxTest.onFx(
        () -> robot.lookup(query).tryQueryAs(Label.class).map(Label::getText).orElse(""));
  }
}
