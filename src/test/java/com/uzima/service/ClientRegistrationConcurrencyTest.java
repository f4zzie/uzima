package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.user.Role;
import com.uzima.persistence.JdbcClientRepository;
import com.uzima.persistence.TestDatabases.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Registers clients from many threads at once through the real service. Client IDs must be unique
 * and gap-free per category. Runs on H2, and on MySQL when Docker is available.
 */
class ClientRegistrationConcurrencyTest {

  private static final int THREADS = 8;
  private static final int PER_THREAD = 12;

  @ParameterizedTest
  @EnumSource(Kind.class)
  void concurrentRegistrationsGetUniqueGapFreeIds(Kind kind) throws Exception {
    try (ServiceTestContext ctx = ServiceTestContext.open(kind)) {
      ctx.signInAs(Role.CLERK);
      ClientService service =
          new ClientService(
              new JdbcClientRepository(ctx.jdbc),
              ctx.sequences,
              ctx.tx,
              ctx.session,
              ctx.audit,
              ctx.clock);
      ClientCategory[] categories = ClientCategory.values();

      ExecutorService pool = Executors.newFixedThreadPool(THREADS);
      List<Callable<List<Client>>> tasks = new ArrayList<>();
      for (int t = 0; t < THREADS; t++) {
        int thread = t;
        tasks.add(
            () -> {
              List<Client> mine = new ArrayList<>();
              for (int i = 0; i < PER_THREAD; i++) {
                ClientCategory category = categories[(thread + i) % categories.length];
                mine.add(
                    service.register(
                        ClientServiceTest.details("Client " + thread + "-" + i, category)));
              }
              return mine;
            });
      }
      List<Client> all = new ArrayList<>();
      try {
        for (Future<List<Client>> f : pool.invokeAll(tasks)) {
          all.addAll(f.get());
        }
      } finally {
        pool.shutdownNow();
      }

      assertThat(all).hasSize(THREADS * PER_THREAD);
      assertThat(all.stream().map(c -> c.code().value())).doesNotHaveDuplicates();
      Map<ClientCategory, List<Integer>> byCategory =
          all.stream()
              .collect(
                  Collectors.groupingBy(
                      c -> c.code().category(),
                      Collectors.mapping(c -> c.code().sequence(), Collectors.toList())));
      byCategory.forEach(
          (category, numbers) ->
              assertThat(numbers.stream().sorted().toList())
                  .as(category.name())
                  .containsExactlyElementsOf(
                      IntStream.rangeClosed(1, numbers.size()).boxed().toList()));
      assertThat(
              ctx.auditEntries().stream()
                  .filter(e -> e.action() == AuditAction.CLIENT_CREATED)
                  .count())
          .isEqualTo(THREADS * PER_THREAD);
    }
  }
}
