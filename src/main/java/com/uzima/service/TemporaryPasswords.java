package com.uzima.service;

import java.security.SecureRandom;

/**
 * Generates one-time passwords for admin-created and admin-reset accounts. The alphabet leaves out
 * look-alike characters (0/O, 1/l/I) because the admin reads the password out to the user.
 */
public final class TemporaryPasswords {

  public static final int LENGTH = 12;
  static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

  private final SecureRandom random;

  public TemporaryPasswords() {
    this(new SecureRandom());
  }

  TemporaryPasswords(SecureRandom random) {
    this.random = random;
  }

  public String generate() {
    StringBuilder sb = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return sb.toString();
  }
}
