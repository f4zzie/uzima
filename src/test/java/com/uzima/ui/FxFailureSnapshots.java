package com.uzima.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;
import org.testfx.api.FxToolkit;

/** Auto-registered for TestFX fixtures and component tests that construct their own FxRobot. */
public final class FxFailureSnapshots
    implements TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler,
        TestWatcher {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(FxFailureSnapshots.class);

  @Override
  public void handleTestExecutionException(ExtensionContext context, Throwable failure)
      throws Throwable {
    capture(context, failure);
    throw failure;
  }

  @Override
  public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable failure)
      throws Throwable {
    capture(context, failure);
    throw failure;
  }

  @Override
  public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable failure)
      throws Throwable {
    capture(context, failure);
    throw failure;
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable failure) {
    // Also covers extension callbacks, such as @Start or the keyboard readiness barrier.
    capture(context, failure);
  }

  static void capture(ExtensionContext context, Throwable failure) {
    if (context.getStore(NAMESPACE).get("captured") != null) {
      return;
    }
    Stage primary = FxToolkit.toolkitContext().getRegisteredStage();
    if (primary == null) {
      return;
    }
    context.getStore(NAMESPACE).put("captured", true);
    try {
      List<Window> windows =
          FxTest.onFx(
              () -> {
                List<Window> result = new ArrayList<>(Window.getWindows());
                if (!result.contains(primary)) {
                  result.add(primary);
                }
                return result;
              });
      String name =
          context.getRequiredTestClass().getSimpleName()
              + "-"
              + context.getDisplayName().replaceAll("[^a-zA-Z0-9._-]", "_")
              + "-"
              + UUID.randomUUID();
      for (int i = 0; i < windows.size(); i++) {
        Window window = windows.get(i);
        var scene = FxTest.onFx(window::getScene);
        if (scene != null) {
          var path = UiSnapshots.save(scene, "failures/" + name + "-window-" + i);
          System.err.println("TestFX failure snapshot: " + path.toAbsolutePath());
        }
      }
    } catch (Throwable snapshotFailure) {
      failure.addSuppressed(snapshotFailure);
      System.err.println("Could not capture TestFX failure snapshot: " + snapshotFailure);
    }
  }
}
