package com.uzima.ui.shell;

import com.uzima.domain.user.Permission;
import com.uzima.domain.user.Role;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.scene.Node;

/**
 * One sidebar entry. The app supplies the list; the shell knows nothing about specific screens.
 *
 * @param permission null when every signed-in user may open it
 * @param view builds the page each time it is opened
 */
public record Destination(
    String id, String title, Icon icon, Permission permission, Supplier<Node> view) {

  public Destination {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(icon, "icon");
    Objects.requireNonNull(view, "view");
  }

  public Optional<Permission> requiredPermission() {
    return Optional.ofNullable(permission);
  }

  public boolean visibleTo(Role role) {
    return permission == null || role.has(permission);
  }
}
