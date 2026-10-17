package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteLine;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.job.StatusChange;
import com.uzima.domain.money.Money;
import com.uzima.persistence.TestDatabases.Kind;
import com.uzima.service.StatusHistoryEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JdbcJobRepositoryTest {

  private static final Instant LATER = Instant.parse("2026-10-03T09:30:00Z");

  @ParameterizedTest
  @EnumSource(Kind.class)
  void fullServiceJobRoundTripsExactly(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("Kiambu Flowers Ltd", ClientCategory.INDUSTRIAL);
      Job saved =
          f.save(f.newJob(client, ClientCategory.INDUSTRIAL, f.fullService(), JobFixtures.CREATED));

      assertThat(saved.id()).isPositive();
      assertThat(saved.quote().grandTotal()).isEqualTo(Money.of("684400.00"));
      assertThat(f.jobs.findById(saved.id())).contains(saved);
      assertThat(f.jobs.findByNumber(saved.number())).contains(saved);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void itemisedPlumbingWithMixedDiametersRoundTripsExactly(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("Mama Njeri", ClientCategory.DOMESTIC);
      Job saved =
          f.save(
              f.newJob(client, ClientCategory.DOMESTIC, f.itemisedPlumbing(), JobFixtures.CREATED));

      Job loaded = f.jobs.findById(saved.id()).orElseThrow();
      assertThat(loaded).isEqualTo(saved);
      assertThat(f.count("plumbing_lines")).isEqualTo(3);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void minimalJobRoundTripsExactly(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("Duka la Mama", ClientCategory.COMMERCIAL);
      Job saved =
          f.save(f.newJob(client, ClientCategory.COMMERCIAL, f.tankOnly(), JobFixtures.CREATED));

      assertThat(f.jobs.findById(saved.id())).contains(saved);
      assertThat(saved.quote().lines()).hasSize(3);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void unknownJobIsEmpty(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      assertThat(f.jobs.findById(999)).isEmpty();
      assertThat(f.jobs.summary(999)).isEmpty();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void insertWritesTheFirstHistoryEntry(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job saved =
          f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));

      assertThat(f.jobs.history(saved.id()))
          .containsExactly(
              new StatusHistoryEntry(
                  Optional.empty(), JobStatus.PENDING_SURVEY, "", JobFixtures.CREATED, f.userId));
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void replaceQuoteSwapsLinesPlumbingAndTotals(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job saved =
          f.save(
              f.newJob(client, ClientCategory.DOMESTIC, f.itemisedPlumbing(), JobFixtures.CREATED));

      QuoteSpec edited =
          new QuoteSpec(
              Optional.of(new DrillingSpec(f.drilling("Core"), Metres.of("120.5"))),
              Optional.empty(),
              Optional.of(f.tank(2000)),
              Optional.empty());
      Job requoted = f.engine.requote(saved, edited);
      f.tx.run(() -> f.jobs.replaceQuote(requoted, LATER));

      assertThat(f.jobs.findById(saved.id())).contains(requoted);
      assertThat(f.count("plumbing_lines")).isZero();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void replaceQuoteIsRefusedOnceTheJobHasMovedOn(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job saved =
          f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));
      Job.Transition approved = saved.advanceTo(JobStatus.APPROVED, LATER, f.userId);
      f.tx.run(() -> f.jobs.recordStatusChange(saved.id(), approved.change()));

      // A stale screen still holding the pending job tries to save an edit.
      assertThatThrownBy(() -> f.tx.run(() -> f.jobs.replaceQuote(saved, LATER)))
          .isInstanceOf(IllegalStateException.class);
      assertThat(f.jobs.findById(saved.id()).orElseThrow().status()).isEqualTo(JobStatus.APPROVED);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void statusChangesUpdateTheJobAndHistoryInOrder(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job job =
          f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));
      List<StatusChange> changes = new ArrayList<>();
      Instant at = LATER;
      for (JobStatus next : List.of(JobStatus.APPROVED, JobStatus.IN_PROGRESS)) {
        Job.Transition t = job.advanceTo(next, at, f.userId);
        f.tx.run(() -> f.jobs.recordStatusChange(t.job().id(), t.change()));
        changes.add(t.change());
        job = t.job();
        at = at.plusSeconds(3600);
      }
      Job.Transition cancel = job.cancel("Client withdrew", at, f.userId);
      f.tx.run(() -> f.jobs.recordStatusChange(cancel.job().id(), cancel.change()));

      assertThat(f.jobs.findById(job.id()).orElseThrow()).isEqualTo(cancel.job());
      List<StatusHistoryEntry> history = f.jobs.history(job.id());
      assertThat(history)
          .extracting(StatusHistoryEntry::to)
          .containsExactly(
              JobStatus.PENDING_SURVEY,
              JobStatus.APPROVED,
              JobStatus.IN_PROGRESS,
              JobStatus.CANCELLED);
      assertThat(history.get(1).from()).contains(JobStatus.PENDING_SURVEY);
      assertThat(history.get(3).reason()).isEqualTo("Client withdrew");
      assertThat(history.get(3).at()).isEqualTo(cancel.change().at());
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void staleStatusChangeIsRefused(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job job =
          f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));
      Job.Transition first = job.advanceTo(JobStatus.APPROVED, LATER, f.userId);
      f.tx.run(() -> f.jobs.recordStatusChange(job.id(), first.change()));

      // Two screens approved the same pending job; the second must not write a second history row.
      assertThatThrownBy(() -> f.tx.run(() -> f.jobs.recordStatusChange(job.id(), first.change())))
          .isInstanceOf(IllegalStateException.class);
      assertThat(f.jobs.history(job.id())).hasSize(2);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void notesCanBeUpdated(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job job =
          f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));

      f.jobs.updateNotes(job.id(), "  Gate code 4411  ", LATER);

      assertThat(f.jobs.findById(job.id()).orElseThrow().notes()).isEqualTo("Gate code 4411");
      assertThatThrownBy(() -> f.jobs.updateNotes(999, "x", LATER))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void writesOutsideATransactionAreRefused(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job job = f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED);

      assertThatThrownBy(() -> f.jobs.insert(job)).isInstanceOf(IllegalStateException.class);
      assertThat(f.count("jobs")).isZero();
    }
  }

  /**
   * The jobs row and the tier snapshot are written before the lines; a line that the database
   * rejects must take every earlier row of the job with it.
   */
  @ParameterizedTest
  @EnumSource(Kind.class)
  void failurePartWayThroughInsertRollsBackTheWholeJob(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      Job valid =
          f.newJob(client, ClientCategory.DOMESTIC, f.itemisedPlumbing(), JobFixtures.CREATED);
      Job broken = withOverlongLastLine(valid);

      assertThatThrownBy(() -> f.tx.run(() -> f.jobs.insert(broken)))
          .isInstanceOf(PersistenceException.class);

      for (String table :
          List.of("jobs", "job_lines", "plumbing_lines", "job_rate_tiers", "job_status_history")) {
        assertThat(f.count(table)).as(table).isZero();
      }
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void retiredCatalogItemsComeBackAsSnapshots(Kind kind) {
    try (JobFixtures f = new JobFixtures(kind)) {
      long client = f.client("A", ClientCategory.DOMESTIC);
      TankType tank = f.tank(1000);
      Job saved =
          f.save(f.newJob(client, ClientCategory.DOMESTIC, f.tankOnly(), JobFixtures.CREATED));
      f.jdbc.update("UPDATE tank_types SET active = FALSE, price = 99999 WHERE id = ?", tank.id());

      TankType loaded = f.jobs.findById(saved.id()).orElseThrow().spec().tank().orElseThrow();
      assertThat(loaded).isEqualTo(tank);
      assertThat(loaded.price()).isEqualTo(Money.of(15_000));
    }
  }

  private static Job withOverlongLastLine(Job job) {
    Quote q = job.quote();
    List<QuoteLine> lines = new ArrayList<>(q.lines());
    QuoteLine last = lines.removeLast();
    lines.add(
        new QuoteLine(
            last.category(),
            "x".repeat(201),
            last.quantity(),
            last.unit(),
            last.unitPrice(),
            last.lineTotal()));
    return new Job(
        job.id(),
        job.number(),
        job.clientId(),
        job.status(),
        job.spec(),
        new Quote(lines, q.subtotal(), q.taxRate(), q.tax(), q.grandTotal()),
        job.priceBasis(),
        job.notes(),
        job.createdAt(),
        job.createdBy());
  }
}
