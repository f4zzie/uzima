package com.uzima.service;

import java.util.function.Supplier;

/**
 * Transaction boundary owned by services. Everything a service does inside {@code work}, including
 * repository writes and audit entries, commits together or rolls back together. Nested calls join
 * the outer transaction.
 */
public interface Transactions {

  <T> T inTransaction(Supplier<T> work);

  default void run(Runnable work) {
    inTransaction(
        () -> {
          work.run();
          return null;
        });
  }
}
