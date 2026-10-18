package com.uzima.ui.component;

import com.uzima.ui.FxTest;
import com.uzima.ui.UiTheme;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.api.FxToolkit;

/**
 * Shows a component in the headless primary stage, so focus and key events behave as in the app.
 */
final class ComponentStage {

  private ComponentStage() {}

  static Scene show(Parent root) {
    FxTest.startToolkit();
    Scene scene =
        FxTest.onFx(
            () -> {
              Stage stage = FxToolkit.toolkitContext().getRegisteredStage();
              Scene s = UiTheme.apply(new Scene(root, 1000, 700));
              stage.setScene(s);
              stage.show();
              stage.toFront();
              stage.requestFocus();
              return s;
            });
    FxTest.settle();
    return scene;
  }
}
