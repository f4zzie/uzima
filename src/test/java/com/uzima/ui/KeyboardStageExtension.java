package com.uzima.ui;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testfx.api.FxToolkit;

/** Shared input barrier, after TestFX's @Start and the test's @BeforeEach setup. */
public final class KeyboardStageExtension implements BeforeTestExecutionCallback {

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    try {
      FxTest.ensureStageReadyForInput(FxToolkit.toolkitContext().getRegisteredStage());
    } catch (RuntimeException | AssertionError failure) {
      FxFailureSnapshots.capture(context, failure);
      throw failure;
    }
  }
}
