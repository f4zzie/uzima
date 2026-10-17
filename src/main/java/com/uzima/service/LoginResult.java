package com.uzima.service;

import com.uzima.domain.user.User;

/** Outcome of a sign-in attempt. */
public sealed interface LoginResult {

  /**
   * The only failure text shown to users. It is the same for an unknown username, a wrong password,
   * a deactivated account and a locked account, so the screen never confirms that a username
   * exists.
   */
  String FAILURE_MESSAGE =
      "Incorrect username or password, or the account is temporarily unavailable. Try again, or"
          + " ask an administrator.";

  /** Signed in. When {@code mustChangePassword} is true the UI must show the change screen next. */
  record Success(User user, boolean mustChangePassword) implements LoginResult {}

  record Failure(String message) implements LoginResult {}
}
