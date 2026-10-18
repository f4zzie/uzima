package com.uzima.ui;

import com.uzima.app.Services;
import com.uzima.service.Session;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.application.Platform;

/**
 * The UI's only way to reach the services. Work runs on a background thread and results come back
 * on the JavaFX thread, so a slow query never freezes the window.
 *
 * <p>Every call passes through {@link #requireOffFxThread()}; a service call made on the FX thread
 * fails immediately instead of quietly blocking the UI.
 */
public final class Backend implements AutoCloseable {

  private final Services services;
  private final ExecutorService worker;
  private final Executor fx;

  public Backend(Services services) {
    this(services, newWorker(), Platform::runLater);
  }

  Backend(Services services, ExecutorService worker, Executor fx) {
    this.services = Objects.requireNonNull(services);
    this.worker = worker;
    this.fx = fx;
  }

  /**
   * Runs {@code work} in the background, then calls exactly one of the callbacks on the FX thread.
   * Exceptions are unwrapped, so {@code onFailure} receives the service's own exception.
   */
  public <T> CompletableFuture<T> run(
      Function<Services, T> work,
      Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onFailure) {
    CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> call(work), worker);
    future.whenCompleteAsync(
        (value, error) -> {
          if (error == null) {
            onSuccess.accept(value);
          } else {
            onFailure.accept(unwrap(error));
          }
        },
        fx);
    return future;
  }

  /** Calls a service on the current thread, which must not be the FX thread. */
  public <T> T call(Function<Services, T> work) {
    requireOffFxThread();
    return work.apply(services);
  }

  /**
   * The signed-in user state. Reading it is an in-memory lookup, so it is safe on the FX thread;
   * signing in or out still goes through {@link #run}.
   */
  public Session session() {
    return services.session();
  }

  /** Throws if called on the JavaFX application thread. */
  public static void requireOffFxThread() {
    if (Platform.isFxApplicationThread()) {
      throw new IllegalStateException(
          "Service called on the JavaFX application thread; use Backend.run instead");
    }
  }

  public static Throwable unwrap(Throwable error) {
    Throwable e = error;
    while ((e instanceof CompletionException || e instanceof ExecutionException)
        && e.getCause() != null) {
      e = e.getCause();
    }
    return e;
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }

  private static ExecutorService newWorker() {
    AtomicInteger count = new AtomicInteger();
    return Executors.newFixedThreadPool(
        2,
        r -> {
          Thread t = new Thread(r, "uzima-worker-" + count.incrementAndGet());
          t.setDaemon(true);
          return t;
        });
  }
}
