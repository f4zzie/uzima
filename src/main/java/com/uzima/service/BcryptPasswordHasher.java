package com.uzima.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;

/**
 * BCrypt ($2a$) at cost 12 by default. Passwords longer than BCrypt's 72-byte limit are pre-hashed
 * with SHA-512 rather than rejected or silently truncated.
 */
public final class BcryptPasswordHasher implements PasswordHasher {

  public static final int DEFAULT_COST = 12;

  private static final BCrypt.Version VERSION = BCrypt.Version.VERSION_2A;
  private static final LongPasswordStrategy LONG_PASSWORDS =
      LongPasswordStrategies.hashSha512(VERSION);

  private final int cost;

  public BcryptPasswordHasher() {
    this(DEFAULT_COST);
  }

  /** Lower costs are for tests only; they make hashing fast and weak. */
  public BcryptPasswordHasher(int cost) {
    if (cost < BCrypt.MIN_COST || cost > BCrypt.MAX_COST) {
      throw new IllegalArgumentException("BCrypt cost out of range: " + cost);
    }
    this.cost = cost;
  }

  @Override
  public String hash(char[] password) {
    return BCrypt.with(VERSION, LONG_PASSWORDS).hashToString(cost, password);
  }

  @Override
  public boolean matches(char[] password, String hash) {
    if (password == null || hash == null) {
      return false;
    }
    try {
      return BCrypt.verifyer(VERSION, LONG_PASSWORDS).verify(password, hash.toCharArray()).verified;
    } catch (IllegalArgumentException malformed) {
      return false;
    }
  }
}
