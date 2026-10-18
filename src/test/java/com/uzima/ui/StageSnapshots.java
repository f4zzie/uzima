package com.uzima.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.Stage;

/** Saves a test stage's scene at each standard size, after resizing the (headless) window. */
public final class StageSnapshots {

  private StageSnapshots() {}

  public static List<Path> saveAtStandardSizes(Stage stage, String name) {
    List<Path> files = new ArrayList<>();
    for (int[] size : UiSnapshots.SIZES) {
      FxTest.onFx(
          () -> {
            stage.setWidth(size[0]);
            stage.setHeight(size[1]);
          });
      FxTest.settle();
      files.add(UiSnapshots.save(stage.getScene(), name + "-" + size[0] + "x" + size[1]));
    }
    return files;
  }
}
