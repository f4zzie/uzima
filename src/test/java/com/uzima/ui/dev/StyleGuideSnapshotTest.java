package com.uzima.ui.dev;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import com.uzima.ui.SampleData;
import com.uzima.ui.UiSnapshots;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StyleGuideSnapshotTest {

  @BeforeAll
  static void start() {
    FxTest.startToolkit();
    SampleData.shared();
  }

  @Test
  void rendersAtStandardSizes() {
    assertThat(UiSnapshots.saveAtStandardSizes("style-guide", StyleGuide::scrollable))
        .allMatch(Files::exists);
  }

  @Test
  void rendersFullLength() {
    assertThat(UiSnapshots.saveFullHeight("style-guide", StyleGuide.content(), 1366)).exists();
  }
}
