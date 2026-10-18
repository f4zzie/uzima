package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackendTest {

  private static UiTestBackend ui;

  @BeforeAll
  static void start() {
    FxTest.startToolkit();
    ui = new UiTestBackend();
  }

  @AfterAll
  static void stop() {
    ui.close();
  }

  @Test
  void serviceCallOnTheFxThreadFails() {
    assertThatThrownBy(() -> FxTest.onFx(() -> ui.backend.call(s -> s.auth().needsAdministrator())))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JavaFX application thread");
  }

  @Test
  void runDoesTheWorkOffTheFxThreadAndReportsBackOnIt() throws Exception {
    AtomicBoolean workOnFx = new AtomicBoolean(true);
    CompletableFuture<Boolean> callbackOnFx = new CompletableFuture<>();
    FxTest.onFx(
        () ->
            ui.backend.run(
                s -> {
                  workOnFx.set(Platform.isFxApplicationThread());
                  return s.auth().needsAdministrator();
                },
                needsAdmin -> callbackOnFx.complete(Platform.isFxApplicationThread()),
                callbackOnFx::completeExceptionally));

    assertThat(callbackOnFx.get(10, TimeUnit.SECONDS)).isTrue();
    assertThat(workOnFx).isFalse();
  }

  @Test
  void failuresArriveUnwrapped() throws Exception {
    CompletableFuture<Throwable> failure = new CompletableFuture<>();
    AtomicReference<Object> success = new AtomicReference<>();
    ui.backend.run(
        s -> s.auth().createAdministrator("x", "", "short".toCharArray()),
        success::set,
        failure::complete);

    assertThat(failure.get(10, TimeUnit.SECONDS)).isInstanceOf(ValidationException.class);
    assertThat(success).hasNullValue();
  }
}
