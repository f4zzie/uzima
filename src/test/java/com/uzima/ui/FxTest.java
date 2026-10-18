package com.uzima.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

/** Starts the headless JavaFX toolkit once and runs code on the FX thread for tests. */
public final class FxTest {

  private FxTest() {}

  public static void startToolkit() {
    try {
      FxToolkit.registerPrimaryStage();
    } catch (TimeoutException e) {
      throw new IllegalStateException("JavaFX toolkit did not start", e);
    }
    UiTheme.loadFonts();
  }

  /** Runs {@code work} on the FX thread and returns its result, rethrowing its exception. */
  public static <T> T onFx(Callable<T> work) {
    startToolkit();
    try {
      return WaitForAsyncUtils.asyncFx(work).get(30, TimeUnit.SECONDS);
    } catch (java.util.concurrent.ExecutionException e) {
      if (e.getCause() instanceof RuntimeException re) {
        throw re;
      }
      throw new IllegalStateException(e.getCause());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static void onFx(Runnable work) {
    onFx(
        () -> {
          work.run();
          return null;
        });
  }

  /** Waits until queued FX events (including Backend callbacks) have run. */
  public static void settle() {
    WaitForAsyncUtils.waitForFxEvents();
  }
}
