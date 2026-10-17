package com.uzima.service;

import com.uzima.domain.user.Permission;
import com.uzima.domain.user.User;
import java.util.Optional;

/**
 * The signed-in user for this desktop session. One instance is created by the composition root and
 * injected into every service; there is no static "current user".
 */
public final class Session {

  private volatile User user;

  public Optional<User> currentUser() {
    return Optional.ofNullable(user);
  }

  public User requireUser() {
    User u = user;
    if (u == null) {
      throw new NotSignedInException();
    }
    return u;
  }

  /** Returns the signed-in user if their role grants {@code permission}; throws otherwise. */
  public User require(Permission permission) {
    User u = requireUser();
    u.role().require(permission);
    return u;
  }

  public boolean has(Permission permission) {
    User u = user;
    return u != null && u.role().has(permission);
  }

  void signIn(User signedIn) {
    this.user = signedIn;
  }

  /** Replaces the cached user after their own record changed (e.g. password or theme). */
  void refresh(User updated) {
    if (user != null && user.id() == updated.id()) {
      this.user = updated;
    }
  }

  void signOut() {
    this.user = null;
  }
}
