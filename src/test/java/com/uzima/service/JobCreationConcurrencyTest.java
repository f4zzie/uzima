package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.job.Job;
import com.uzima.domain.user.Role;
import com.uzima.persistence.TestDatabases.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Job numbers stay unique and gap-free when several users save quotes at the same time. */
class JobCreationConcurrencyTest {

  private static final int THREADS = 8;
  private static final int PER_THREAD = 5;

  @ParameterizedTest
  @EnumSource(Kind.class)
  void concurrentJobCreationGetsUniqueGapFreeNumbers(Kind kind) throws Exception {
    try (JobServiceFixture f = JobServiceFixture.open(kind)) {
      f.signInAs(Role.CLERK);
      Client client = f.client(ClientCategory.DOMESTIC);
      var spec = f.basicDomestic("80");

      ExecutorService pool = Executors.newFixedThreadPool(THREADS);
      List<Callable<List<Job>>> tasks = new ArrayList<>();
      for (int t = 0; t < THREADS; t++) {
        tasks.add(
            () -> {
              List<Job> mine = new ArrayList<>();
              for (int i = 0; i < PER_THREAD; i++) {
                mine.add(f.jobs.create(client.id(), spec, ""));
              }
              return mine;
            });
      }
      List<Job> all = new ArrayList<>();
      try {
        for (Future<List<Job>> future : pool.invokeAll(tasks)) {
          all.addAll(future.get());
        }
      } finally {
        pool.shutdownNow();
      }

      assertThat(all.stream().map(j -> j.number().sequence()).sorted())
          .containsExactlyElementsOf(
              IntStream.rangeClosed(1, THREADS * PER_THREAD).boxed().toList());
      assertThat(f.count("jobs")).isEqualTo(THREADS * PER_THREAD);
    }
  }
}
