package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Amounts must line up digit for digit in columns, which plain Inter doesn't do. */
class TabularFiguresTest {

  private static final List<String> AMOUNTS =
      List.of("1,111.11", "8,888.88", "10,000.00", "684,400.00", "94,400.00");

  @BeforeAll
  static void start() {
    FxTest.startToolkit();
  }

  @Test
  void interNumHasEqualWidthDigits() {
    for (String family : List.of("Inter Num", "Inter Num Medium", "Inter Num SemiBold")) {
      assertThat(FxTest.onFx(() -> width("1,111.11", family)))
          .as(family)
          .isEqualTo(FxTest.onFx(() -> width("8,888.88", family)));
    }
  }

  @Test
  void plainInterDoesNotWhichIsWhyInterNumExists() {
    assertThat(FxTest.onFx(() -> width("1,111.11", "Inter Medium")))
        .isLessThan(FxTest.onFx(() -> width("8,888.88", "Inter Medium")));
  }

  @Test
  void rendersAColumnOfAmountsForVisualCheck() {
    assertThat(UiSnapshots.save("tabular-figures", column(), 520, 260)).exists();
  }

  private static double width(String text, String family) {
    Font font = Font.font(family, 14);
    assertThat(font.getFamily()).isEqualTo(family);
    Text t = new Text(text);
    t.setFont(font);
    return t.getLayoutBounds().getWidth();
  }

  private static HBox column() {
    HBox row =
        new HBox(
            48,
            amounts("Inter (proportional)", "text-body"),
            amounts("Inter Num (tabular)", "text-numeric"));
    row.getStyleClass().add("page");
    return row;
  }

  private static VBox amounts(String caption, String styleClass) {
    Label heading = new Label(caption);
    heading.getStyleClass().add("text-caption");
    VBox box = new VBox(8, heading);
    box.setAlignment(Pos.TOP_RIGHT);
    for (String amount : AMOUNTS) {
      Label l = new Label(amount);
      l.getStyleClass().addAll("text-subtitle", styleClass);
      box.getChildren().add(l);
    }
    box.setPrefWidth(180);
    return box;
  }
}
