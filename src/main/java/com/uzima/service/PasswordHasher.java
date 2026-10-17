package com.uzima.service;

/**
 * One-way password hashing. Passwords travel as {@code char[]} so callers can clear them after use;
 * implementations never log either the password or the hash.
 */
public interface PasswordHasher {

  String hash(char[] password);

  /** False for a wrong password and for a hash this hasher cannot read. */
  boolean matches(char[] password, String hash);
}
