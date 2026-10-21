package com.uzima.ui;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import javafx.scene.Node;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

/** Waiting helpers for flows whose results arrive from the background thread. */
public final class FlowTestSupport {

  private FlowTestSupport() {}

  public static void waitUntil(BooleanSupplier condition) {
    try {
      WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, condition::getAsBoolean);
    } catch (TimeoutException e) {
      throw new AssertionError("Condition not met within 15 seconds", e);
    }
    WaitForAsyncUtils.waitForFxEvents();
  }

  /** Waits until a node matching {@code query} is in the scene and visible. */
  public static <T extends Node> T waitFor(FxRobot robot, String query) {
    waitUntil(() -> robot.lookup(query).tryQuery().filter(Node::isVisible).isPresent());
    return robot.lookup(query).query();
  }
}
