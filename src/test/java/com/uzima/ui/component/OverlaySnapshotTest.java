package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.ui.FxTest;
import com.uzima.ui.UiSnapshots;
import java.nio.file.Files;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Renders a live host with an open dialog and toasts over sample content. */
class OverlaySnapshotTest {

  @BeforeAll
  static void start() {
    FxTest.startToolkit();
  }

  @Test
  void dialogOverContent() {
    assertThat(UiSnapshots.saveAtStandardSizes("overlay-dialog", OverlaySnapshotTest::host))
        .allMatch(Files::exists);
  }

  private static OverlayHost host() {
    VBox page = new VBox();
    page.getStyleClass().add("page");
    Label heading = new Label("Clients");
    heading.getStyleClass().add("text-headline");
    page.getChildren().add(heading);
    for (int i = 1; i <= 8; i++) {
      Label row = new Label("UZM-DOM-000" + i + "   Client " + i + "   Machakos");
      row.getStyleClass().add("card");
      row.setMaxWidth(Double.MAX_VALUE);
      page.getChildren().add(row);
    }
    OverlayHost host = new OverlayHost(page);

    TextField name = new TextField("Wanjiru Kamau");
    TextArea notes = new TextArea("Prefers calls after 5 pm.");
    notes.setPrefRowCount(3);
    FormField phone = new FormField("Phone", new TextField("0712"));
    phone.setError("Enter a full Kenyan phone number, like 0712 345 678");
    VBox form = new VBox(new FormField("Client name", name), phone, new FormField("Notes", notes));
    form.getStyleClass().add("stack-16");
    DialogShell dialog = new DialogShell("Edit client");
    dialog.setSubtitle("UZM-DOM-0007 · Domestic");
    dialog.setContent(form);
    host.showDialog(dialog);
    host.success("Client saved", "UZM-DOM-0006 · Achieng Otieno");
    return host;
  }
}
