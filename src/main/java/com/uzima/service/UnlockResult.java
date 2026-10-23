package com.uzima.service;

/** Outcome of a password check on the idle lock screen. */
public enum UnlockResult {
  /** The password matched; the session continues. */
  UNLOCKED,
  /** The password was wrong; the user may try again. */
  WRONG_PASSWORD,
  /**
   * The account is now locked or deactivated. The session has been ended and the UI must return to
   * the sign-in screen showing {@link LoginResult#FAILURE_MESSAGE}.
   */
  SESSION_ENDED
}