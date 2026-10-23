package com.uzima.service;

import com.uzima.domain.user.User;

/**
 * A newly created or reset account plus its one-time password. The password is shown to the admin
 * once and is not stored anywhere in plain text.
 */
public record CreatedUser(User user, String temporaryPassword) {

  @Override
  public String toString() {
    return "CreatedUser[user=" + user + ", temporaryPassword=<hidden>]";
  }
}