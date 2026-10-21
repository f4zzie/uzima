package com.uzima.ui.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.service.ClientSummary;
import com.uzima.ui.FxTest;
import com.uzima.ui.SampleData;
import com.uzima.ui.UiSnapshots;
import java.nio.file.Files;
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
    SampleData.shared();
  }

  @Test
  void dialogOverContent() {
    assertThat(UiSnapshots.saveAtStandardSizes("overlay-dialog", OverlaySnapshotTest::host))
        .allMatch(Files::exists);
  }

  /** The sample client list with the edit dialog for Wanjiru Kamau open over it. */
  private static OverlayHost host() {
    DataTable<ClientSummary> table = new DataTable<>();
    table.setItemNouns("client", "clients");
    table.addFiguresColumn("Client ID", c -> c.code().value(), ColumnWidth.fixed(160));
    table.addTextColumn("Name", ClientSummary::name, ColumnWidth.flex(220));
    table.addTextColumn("County", c -> c.county().label(), ColumnWidth.fixed(150));
    table.setItems(SampleData.shared().clientSummaries());
    VBox page = new VBox(table);
    page.getStyleClass().add("page");
    VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
    OverlayHost host = new OverlayHost(page);

    TextField name = new TextField("Wanjiru Kamau");
    TextArea address = new TextArea("Kamiti Road, Ruiru");
    address.setPrefRowCount(2);
    FormField phone = new FormField("Phone", new TextField("0712 345"));
    phone.setError("Enter a full Kenyan phone number, like 0712 345 678");
    VBox form = new VBox(new FormField("Name", name), phone, new FormField("Address", address));
    form.getStyleClass().add("stack-16");
    DialogShell dialog = new DialogShell("Edit client");
    dialog.setSubtitle("UZM-DOM-0001 · Domestic");
    dialog.setContent(form);
    dialog.setPrimary("Save changes", false);
    host.showDialog(dialog);
    return host;
  }
}
