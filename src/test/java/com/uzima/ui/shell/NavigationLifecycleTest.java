package com.uzima.ui.shell;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import com.uzima.ui.KeyboardStageExtension;
import com.uzima.ui.SampleData;
import com.uzima.ui.UiTheme;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.OverlayHost;
import com.uzima.ui.component.StatePane;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith({ApplicationExtension.class, KeyboardStageExtension.class})
class NavigationLifecycleTest {
  private Stage stage;
  private AppShell shell;
  private DeferredPage initial;

  @BeforeAll
  static void seed() {
    SampleData.shared();
  }

  @Start
  void start(Stage stage) {
    this.stage = stage;
    var backend = SampleData.shared().ui.backend;
    initial = new DeferredPage();
    initial.ready();
    shell =
        new AppShell(
            new ShellViewModel(
                backend,
                backend.session().requireUser(),
                List.of(new Destination("first", "First", Icon.JOBS, null, nav -> initial.root)),
                null,
                () -> {}));
    stage.setScene(UiTheme.apply(new Scene(new OverlayHost(shell.root()), 1366, 768)));
    FxTest.traceFocus(stage.getScene());
    stage.show();
  }

  @AfterEach
  void hide() {
    FxTest.onFx(stage::hide);
  }

  @Test
  void readyBeforeSceneAttachmentStillReceivesFocusExactlyOnce() throws Exception {
    FxTest.waitForFocus(stage, initial.first);
    afterPulse();
    assertThat(FxTest.onFx(() -> initial.requests)).isEqualTo(1);
  }

  @Test
  void loadingThenReadyFocusesOnceAndLaterRenderingCannotStealIt() throws Exception {
    DeferredPage detail = pushLoading();
    afterPulse();
    assertThat(FxTest.onFx(() -> detail.requests)).isZero();
    FxTest.onFx(detail::ready);
    FxTest.waitForFocus(stage, detail.first);
    assertThat(FxTest.onFx(() -> detail.requests)).isEqualTo(1);

    FxTest.onFx(
        () -> {
          detail.second.requestFocus();
          detail.ready();
        });
    afterPulse();
    FxTest.waitForFocus(stage, detail.second);
    assertThat(FxTest.onFx(() -> detail.requests)).isEqualTo(1);
  }

  @Test
  void readyBeforeTheScrollPaneSkinExistsWaitsForSceneAttachment() {
    FxTest.waitForFocus(stage, initial.first);
    DeferredPage detail =
        FxTest.onFx(
            () -> {
              DeferredPage page = new DeferredPage();
              shell.navigator().push("Fast load", page.root);
              page.ready();
              assertThat(page.first.getScene()).as("content before the first CSS pass").isNull();
              assertThat(page.requests).isZero();
              return page;
            });
    FxTest.waitForFocus(stage, detail.first);
    assertThat(FxTest.onFx(() -> detail.requests)).isEqualTo(1);
  }

  @Test
  void aLateReadyCallbackCannotFocusAPageThatWasLeft() throws Exception {
    DeferredPage detail = pushLoading();
    FxTest.onFx(
        () -> {
          shell.navigator().back();
          detail.ready();
        });
    FxTest.waitForFocus(stage, initial.first);
    afterPulse();
    assertThat(FxTest.onFx(() -> detail.requests)).isZero();
    assertThat(FxTest.onFx(() -> initial.requests)).isEqualTo(2);
  }

  @Test
  void updatingTheCurrentPagesTitleDoesNotStartAnotherFocusRequest() throws Exception {
    DeferredPage detail = pushLoading();
    FxTest.onFx(detail::ready);
    FxTest.waitForFocus(stage, detail.first);
    FxTest.onFx(
        () -> {
          detail.second.requestFocus();
          shell.navigator().replace("Loaded title", detail.root);
        });
    afterPulse();
    FxTest.waitForFocus(stage, detail.second);
    assertThat(FxTest.onFx(() -> detail.requests)).isEqualTo(1);
  }

  @Test
  void aPageThatBecomesReadyUnderADialogWaitsUntilTheDialogCloses() throws Exception {
    DeferredPage detail = pushLoading();
    DialogShell dialog =
        FxTest.onFx(
            () -> {
              DialogShell modal = new DialogShell("Confirm");
              shell.navigator().overlays().showDialog(modal);
              detail.ready();
              return modal;
            });
    afterPulse();
    FxTest.waitForFocus(stage, dialog.primaryButton());
    assertThat(FxTest.onFx(() -> detail.requests)).isZero();

    FxTest.onFx(dialog::cancel);
    FxTest.waitForFocus(stage, detail.first);
    afterPulse();
    assertThat(FxTest.onFx(() -> detail.requests)).isEqualTo(1);
  }

  @Test
  void aPageThatBecomesReadyUnderALoadingOverlayWaitsUntilItCloses() throws Exception {
    DeferredPage detail = pushLoading();
    FxTest.onFx(
        () -> {
          shell.navigator().overlays().loading().show("Working");
          detail.ready();
        });
    afterPulse();
    assertThat(FxTest.onFx(() -> detail.requests)).isZero();

    FxTest.onFx(() -> shell.navigator().overlays().loading().hide());
    FxTest.waitForFocus(stage, detail.first);
    afterPulse();
    assertThat(FxTest.onFx(() -> detail.requests)).isEqualTo(1);
  }

  private DeferredPage pushLoading() {
    FxTest.waitForFocus(stage, initial.first);
    return FxTest.onFx(
        () -> {
          DeferredPage page = new DeferredPage();
          shell.navigator().push("Detail", page.root);
          return page;
        });
  }

  private void afterPulse() throws Exception {
    CountDownLatch pulse = new CountDownLatch(1);
    Scene scene = FxTest.onFx(stage::getScene);
    Runnable listener = pulse::countDown;
    FxTest.onFx(
        () -> {
          scene.addPostLayoutPulseListener(listener);
          Platform.requestNextPulse();
        });
    try {
      assertThat(pulse.await(5, TimeUnit.SECONDS)).as("a rendered scene pulse").isTrue();
    } finally {
      FxTest.onFx(() -> scene.removePostLayoutPulseListener(listener));
    }
  }

  private static final class DeferredPage implements InitialFocus {
    final TextField first = new TextField();
    final TextField second = new TextField();
    final StatePane root = new StatePane(new ScrollPane(new VBox(first, second)));
    int requests;

    DeferredPage() {
      first.setId("initial-control");
      second.setId("user-selected-control");
      InitialFocus.attach(root, this);
      root.showLoading("Loading");
    }

    void ready() {
      root.showContent();
    }

    @Override
    public Node initialFocusTarget() {
      return root.getState() == StatePane.State.CONTENT ? first : null;
    }

    @Override
    public void requestInitialFocus() {
      requests++;
      InitialFocus.super.requestInitialFocus();
    }
  }
}
