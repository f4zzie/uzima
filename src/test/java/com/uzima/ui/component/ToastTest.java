package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

class ToastTest {

  private OverlayHost host;

  @BeforeEach
  void setUp() {
    FxTest.startToolkit();
    host = FxTest.onFx(() -> new OverlayHost(new Label("Dashboard")));
    ComponentStage.show(host);
  }

  @Test
  void atMostThreeToastsShowAndTheOldestGoesFirst() {
    FxTest.onFx(
        () -> {
          for (int i = 1; i <= 5; i++) {
            host.error("Error " + i, "");
          }
        });
    assertThat(host.toasts())
        .extracting(Toast::getTitle)
        .containsExactly("Error 3", "Error 4", "Error 5");
  }

  @Test
  void successAndInfoHideThemselvesButErrorsStay() throws Exception {
    FxTest.onFx(
        () -> {
          host.setToastDuration(Duration.millis(50));
          host.success("Client saved", "UZM-DOM-0007");
          host.info("Prices updated", null);
          host.error("Could not save", "The database is not reachable.");
        });
    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> host.toasts().size() == 1);
    assertThat(host.toasts().get(0).kind()).isEqualTo(Toast.Kind.ERROR);
  }

  @Test
  void dismissRemovesTheToast() {
    Toast toast = FxTest.onFx(() -> host.error("Could not save", "Try again."));
    new FxRobot().clickOn(toast.dismissButton());
    assertThat(host.toasts()).isEmpty();
  }

  @Test
  void toastsAreAnnouncedWithTitleAndMessage() {
    Toast toast = FxTest.onFx(() -> host.success("Payment recorded", "RCP-2026-00012"));
    assertThat(toast.getAccessibleText()).isEqualTo("Payment recorded. RCP-2026-00012");
    assertThat(toast.getMessage()).isEqualTo("RCP-2026-00012");
    assertThat(toast.getStyleClass()).contains("toast-success");
    assertThat(toast.dismissButton().getAccessibleText()).isEqualTo("Dismiss notification");

    Toast titleOnly = FxTest.onFx(() -> host.info("Signed out", ""));
    assertThat(titleOnly.getAccessibleText()).isEqualTo("Signed out");
  }
}
