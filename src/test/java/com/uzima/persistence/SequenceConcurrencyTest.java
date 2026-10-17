package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.persistence.TestDatabases.Kind;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Concurrent callers must get unique, gap-free numbers even when some of their transactions roll
 * back. Runs on H2 always and on MySQL when Docker is available.
 */
class SequenceConcurrencyTest {

  private static final int THREADS = 8;
  private static final int PER_THREAD = 25;

  @ParameterizedTest
  @EnumSource(Kind.class)
  void concurrentTransactionsGetUniqueGapFreeNumbers(Kind kind) throws Exception {
    try (Database db = TestDatabases.open(kind)) {
      JdbcTransactions tx = new JdbcTransactions(db.dataSource());
      JdbcSequenceGenerator sequences = new JdbcSequenceGenerator(tx, new Jdbc(tx));
      List<Long> committed = Collections.synchronizedList(new ArrayList<>());

      runConcurrently(
          worker -> {
            for (int i = 0; i < PER_THREAD; i++) {
              boolean rollBack = (worker + i) % 5 == 0;
              try {
                tx.run(
                    () -> {
                      long n = sequences.next("TEST");
                      if (rollBack) {
                        throw new IllegalStateException("simulated failure");
                      }
                      committed.add(n);
                    });
              } catch (IllegalStateException expected) {
                // The number taken in this transaction must be handed out again.
              }
            }
            return null;
          });

      long[] expected = LongStream.rangeClosed(1, committed.size()).toArray();
      assertThat(committed).doesNotHaveDuplicates();
      assertThat(committed.stream().mapToLong(Long::longValue).sorted().toArray())
          .containsExactly(expected);
      assertThat(committed.size()).isLessThan(THREADS * PER_THREAD);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void concurrentFirstUseCreatesTheCounterOnce(Kind kind) throws Exception {
    try (Database db = TestDatabases.open(kind)) {
      JdbcTransactions tx = new JdbcTransactions(db.dataSource());
      JdbcSequenceGenerator sequences = new JdbcSequenceGenerator(tx, new Jdbc(tx));
      List<ClientCode> codes = Collections.synchronizedList(new ArrayList<>());

      runConcurrently(
          worker -> {
            codes.add(tx.inTransaction(() -> sequences.nextClientCode(ClientCategory.DOMESTIC)));
            return null;
          });

      assertThat(codes.stream().map(ClientCode::sequence).sorted())
          .containsExactlyElementsOf(IntStream.rangeClosed(1, THREADS).boxed().toList());
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void sequencesAreIndependentPerNameAndYear(Kind kind) {
    try (Database db = TestDatabases.open(kind)) {
      JdbcTransactions tx = new JdbcTransactions(db.dataSource());
      JdbcSequenceGenerator sequences = new JdbcSequenceGenerator(tx, new Jdbc(tx));
      tx.run(
          () -> {
            assertThat(sequences.nextJobNumber(Year.of(2026)).value()).isEqualTo("JOB-2026-00001");
            assertThat(sequences.nextJobNumber(Year.of(2026)).value()).isEqualTo("JOB-2026-00002");
            assertThat(sequences.nextJobNumber(Year.of(2027)).value()).isEqualTo("JOB-2027-00001");
            assertThat(sequences.nextReceiptNumber(Year.of(2026)).value())
                .isEqualTo("RCP-2026-00001");
            assertThat(sequences.nextClientCode(ClientCategory.INDUSTRIAL).value())
                .isEqualTo("UZM-IND-0001");
          });
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void numbersCannotBeTakenOutsideATransaction(Kind kind) {
    try (Database db = TestDatabases.open(kind)) {
      JdbcTransactions tx = new JdbcTransactions(db.dataSource());
      JdbcSequenceGenerator sequences = new JdbcSequenceGenerator(tx, new Jdbc(tx));
      assertThatThrownBy(() -> sequences.nextJobNumber(Year.of(2026)))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  private interface Worker {
    Void run(int worker) throws Exception;
  }

  private static void runConcurrently(Worker work) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(THREADS);
    try {
      List<Callable<Void>> tasks = new ArrayList<>();
      for (int w = 0; w < THREADS; w++) {
        int worker = w;
        tasks.add(() -> work.run(worker));
      }
      for (Future<Void> f : pool.invokeAll(tasks)) {
        f.get();
      }
    } finally {
      pool.shutdownNow();
    }
  }
}
