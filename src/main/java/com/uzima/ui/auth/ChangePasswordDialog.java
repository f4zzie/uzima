package com.uzima.ui.auth;

import com.uzima.ui.Backend;
import com.uzima.ui.component.DialogShell;
import com.uzima.ui.component.FormBinding;
import com.uzima.ui.component.OverlayHost;

/** The voluntary "Change password…" dialog, opened from the account menu. */
public final class ChangePasswordDialog {

  static final String CURRENT_HELP = "The password you signed in with.";

  private ChangePasswordDialog() {}

  /** Builds the dialog; {@code onChanged} runs on the FX thread after it closed successfully. */
  public static DialogShell create(Backend backend, Runnable onChanged) {
    DialogShell dialog = new DialogShell("Change password", DialogShell.Size.SMALL);
    ChangePasswordViewModel model =
        new ChangePasswordViewModel(
            backend,
            () -> {
              dialog.close(DialogShell.Outcome.CONFIRMED);
              onChanged.run();
            });
    ChangePasswordForm form = new ChangePasswordForm(model, CURRENT_HELP);
    form.root().getStyleClass().add("stack-16");
    dialog.setContent(form.root());
    dialog.setPrimary("Change password", false);
    FormBinding.failure(dialog, model.failureProperty());
    model
        .busyProperty()
        .addListener(
            (obs, was, busy) -> {
              dialog.setBusy(busy);
              if (!busy) {
                form.focusFirstError();
              }
            });
    dialog.setOnPrimary(
        d -> {
          model.change();
          form.focusFirstError();
        });
    return dialog;
  }

  /** Opens the dialog over {@code host} and confirms success with a toast. */
  public static void show(OverlayHost host, Backend backend) {
    host.showDialog(
        create(
            backend, () -> host.success("Password changed", "Use it the next time you sign in.")));
  }
}
