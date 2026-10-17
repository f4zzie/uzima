package com.uzima.service;

import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Staff accounts. Usernames never change once created. */
public interface UserRepository {

  /** Saves a new user and returns it with its generated id. */
  User insert(User user, Instant createdAt);

  /** Saves every mutable field: display name, role, hash, flags and login state. */
  void update(User user);

  Optional<User> findById(long id);

  Optional<User> findByUsername(Username username);

  /** All users, active first, then by username. */
  List<User> findAll();

  long count();

  long countActiveAdmins();

  Theme theme(long userId);

  void setTheme(long userId, Theme theme);
}
