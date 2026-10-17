package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.IllegalStatusTransitionException;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteLockedException;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.Role;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobServiceTest {

  private JobServiceFixture f;
  private JobService jobs;
  private Client client;

  @BeforeEach
  void setUp() {
    f = JobServiceFixture.h2();
    jobs = f.jobs;
    f.signInAs(Role.CLERK);
    client = f.client(ClientCategory.DOMESTIC);
  }

  @AfterEach
  void tearDown() {
    f.close();
  }

  private Job createExample2() {
    return jobs.create(client.id(), f.basicDomestic("80"), "Near the gate");
  }

  private Job reload(Job job) {
    return jobs.find(job.id()).orElseThrow();
  }

  private AuditEntry lastAudit() {
    return f.ctx.auditEntries().getFirst();
  }

  // --- Creating --------------------------------------------------------------------------------

  @Test
  void createSavesJobAwaitingSurveyWithNumberSnapshotAndAudit() {
    Job job = createExample2();

    assertThat(job.number().value()).isEqualTo("JOB-2026-00001");
    assertThat(job.status()).isEqualTo(JobStatus.PENDING_SURVEY);
    assertThat(job.quote().subtotal()).isEqualTo(Money.of(257_000));
    assertThat(job.quote().tax()).isEqualTo(Money.of(41_120));
    assertThat(job.quote().grandTotal()).isEqualTo(Money.of(298_120));
    assertThat(reload(job)).isEqualTo(job);
    assertThat(jobs.history(job.id()))
        .singleElement()
        .satisfies(h -> assertThat(h.to()).isEqualTo(JobStatus.PENDING_SURVEY));
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_CREATED);
    assertThat(lastAudit().entityId()).isEqualTo("JOB-2026-00001");
    assertThat(lastAudit().details())
        .contains(client.code().value())
        .contains("grand total: KSh 298,120.00");
  }

  @Test
  void jobNumbersAreSequentialPerYearInNairobiTime() {
    createExample2();
    // 21:30 UTC on 31 Dec 2026 is already 1 Jan 2027 in Nairobi.
    f.ctx.clock.set(Instant.parse("2026-12-31T21:30:00Z"));
    assertThat(createExample2().number().value()).isEqualTo("JOB-2027-00001");
  }

  @Test
  void previewPricesWithoutSaving() {
    int auditBefore = f.ctx.auditEntries().size();
    Quote quote = jobs.preview(client.id(), f.basicDomestic("80"));

    assertThat(quote.grandTotal()).isEqualTo(Money.of(298_120));
    assertThat(f.count("jobs")).isZero();
    assertThat(f.ctx.auditEntries()).hasSize(auditBefore);
  }

  @Test
  void deactivatedClientCannotBeQuoted() {
    f.signInAs(Role.MANAGER);
    f.clients.deactivate(client.id());

    assertThatThrownBy(this::createExample2)
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("deactivated");
    assertThat(f.count("jobs")).isZero();
  }

  @Test
  void unknownClientAndJobAreReported() {
    assertThatThrownBy(() -> jobs.create(9999, f.basicDomestic("80"), ""))
        .isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> jobs.cancel(9999, "x")).isInstanceOf(DomainException.class);
    f.signInAs(Role.MANAGER);
    assertThatThrownBy(() -> jobs.cancel(9999, "x")).isInstanceOf(JobNotFoundException.class);
  }

  // --- Permissions -----------------------------------------------------------------------------

  @Test
  void clerkCannotCancelJob() {
    Job job = createExample2();

    assertThatThrownBy(() -> jobs.cancel(job.id(), "Client withdrew"))
        .isInstanceOfSatisfying(
            AccessDeniedException.class,
            e -> assertThat(e.permission()).isEqualTo(Permission.CANCEL_JOB));
    assertThat(reload(job).status()).isEqualTo(JobStatus.PENDING_SURVEY);
  }

  @Test
  void clerkCanCreateAndAdvance() {
    Job job = createExample2();
    assertThat(jobs.advance(job.id(), JobStatus.APPROVED).status()).isEqualTo(JobStatus.APPROVED);
  }

  @Test
  void managerCanCancelButNotReopen() {
    Job job = createExample2();
    f.signInAs(Role.MANAGER);

    jobs.cancel(job.id(), "Client withdrew");

    assertThat(reload(job).status()).isEqualTo(JobStatus.CANCELLED);
    assertThatThrownBy(() -> jobs.reopen(job.id(), "Back on"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void adminCanReopenCancelledJob() {
    Job job = createExample2();
    f.signInAs(Role.ADMIN);
    jobs.cancel(job.id(), "Client withdrew");

    Job reopened = jobs.reopen(job.id(), "Client found funding");

    assertThat(reopened.status()).isEqualTo(JobStatus.PENDING_SURVEY);
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_REOPENED);
    assertThat(lastAudit().details())
        .isEqualTo("status: Cancelled → Pending survey; reason: Client found funding");
  }

  @Test
  void signedOutCallsAreRefused() {
    Job job = createExample2();
    f.ctx.signOut();

    assertThatThrownBy(() -> jobs.preview(client.id(), f.basicDomestic("10")))
        .isInstanceOf(NotSignedInException.class);
    assertThatThrownBy(() -> jobs.find(job.id())).isInstanceOf(NotSignedInException.class);
    assertThatThrownBy(() -> jobs.search(JobSearch.all())).isInstanceOf(NotSignedInException.class);
    assertThatThrownBy(() -> jobs.advance(job.id(), JobStatus.APPROVED))
        .isInstanceOf(NotSignedInException.class);
  }

  // --- Snapshots (acceptance example 5) --------------------------------------------------------

  @Test
  void taxRateChangeAffectsNewQuotesButNotSavedJobs() {
    Job saved = createExample2();
    f.signInAs(Role.ADMIN);
    f.setTaxRate("0.14");
    f.signInAs(Role.CLERK);

    Job newer = createExample2();

    assertThat(newer.quote().taxRate()).isEqualTo(TaxRate.of("0.14"));
    assertThat(newer.quote().tax()).isEqualTo(Money.of(35_980));
    Job reloaded = reload(saved);
    assertThat(reloaded.quote().taxRate()).isEqualTo(TaxRate.of("0.16"));
    assertThat(reloaded.quote().tax()).isEqualTo(Money.of(41_120));
    assertThat(reloaded.quote().grandTotal()).isEqualTo(Money.of(298_120));
  }

  @Test
  void catalogPriceChangeLeavesSavedJobUnchanged() {
    Job saved = createExample2();
    f.signInAs(Role.MANAGER);
    f.catalog.changeItem(f.drilling("Symmetric").withPrice(Money.of(150_000)));

    assertThat(reload(saved)).isEqualTo(saved);
    assertThat(jobs.preview(client.id(), f.basicDomestic("80")).subtotal())
        .isEqualTo(Money.of(277_000));
  }

  // --- Editing and re-pricing ------------------------------------------------------------------

  @Test
  void editingKeepsSnapshottedPricesAfterCatalogChange() {
    Job saved = createExample2();
    f.signInAs(Role.MANAGER);
    f.catalog.changeItem(f.drilling("Symmetric").withPrice(Money.of(150_000)));

    // Deeper borehole, still at the old 130,000 down payment: 130,000 + 100,000 + 30,000 + 17,000.
    Job edited = jobs.editQuote(saved.id(), f.basicDomestic("100"), saved.notes());

    assertThat(edited.quote().subtotal()).isEqualTo(Money.of(277_000));
    assertThat(edited.quote().grandTotal()).isEqualTo(Money.of(321_320));
    assertThat(reload(saved)).isEqualTo(edited);
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_QUOTE_EDITED);
    assertThat(lastAudit().details()).isEqualTo("grand total: KSh 298,120.00 → KSh 321,320.00");
  }

  @Test
  void unchangedEditWritesNothing() {
    Job saved = createExample2();
    int before = f.ctx.auditEntries().size();
    jobs.editQuote(saved.id(), f.basicDomestic("80"), saved.notes());
    assertThat(f.ctx.auditEntries()).hasSize(before);
  }

  @Test
  void previewRepriceShowsBeforeAndAfterWithoutSaving() {
    Job saved = createExample2();
    f.signInAs(Role.MANAGER);
    f.catalog.changeItem(f.drilling("Symmetric").withPrice(Money.of(150_000)));

    Job.Repricing preview = jobs.previewReprice(saved.id());

    assertThat(preview.beforeQuote().grandTotal()).isEqualTo(Money.of(298_120));
    assertThat(preview.afterQuote().grandTotal()).isEqualTo(Money.of(321_320));
    assertThat(preview.grandTotalChange()).isEqualTo(Money.of(23_200));
    assertThat(reload(saved)).isEqualTo(saved);
  }

  @Test
  void applyRepriceSavesAndAuditsWhenTotalsMatchThePreview() {
    Job saved = createExample2();
    f.signInAs(Role.MANAGER);
    f.catalog.changeItem(f.drilling("Symmetric").withPrice(Money.of(150_000)));
    Money expected = jobs.previewReprice(saved.id()).afterQuote().grandTotal();

    Job repriced = jobs.applyReprice(saved.id(), expected);

    assertThat(reload(saved)).isEqualTo(repriced);
    assertThat(repriced.quote().grandTotal()).isEqualTo(Money.of(321_320));
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_REPRICED);
    assertThat(lastAudit().details()).isEqualTo("grand total: KSh 298,120.00 → KSh 321,320.00");
  }

  @Test
  void applyRepriceIsRefusedWhenPricesMovedSinceThePreview() {
    Job saved = createExample2();
    f.signInAs(Role.MANAGER);
    f.catalog.changeItem(f.drilling("Symmetric").withPrice(Money.of(150_000)));
    Money seen = jobs.previewReprice(saved.id()).afterQuote().grandTotal();
    f.catalog.changeItem(f.drilling("Symmetric").withPrice(Money.of(160_000)));

    assertThatThrownBy(() -> jobs.applyReprice(saved.id(), seen))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("review it again");
    assertThat(reload(saved)).isEqualTo(saved);
  }

  @Test
  void repriceWithNothingChangedIsANoOp() {
    Job saved = createExample2();
    Job.Repricing preview = jobs.previewReprice(saved.id());

    assertThat(preview.changesAnything()).isFalse();
    assertThat(jobs.applyReprice(saved.id(), saved.quote().grandTotal())).isEqualTo(saved);
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_CREATED);
  }

  @Test
  void categoryChangeReachesPendingJobOnlyThroughReprice() {
    Job saved = createExample2();
    f.signInAs(Role.MANAGER);
    f.clients.changeCategory(client.id(), ClientCategory.COMMERCIAL);

    assertThat(reload(saved)).isEqualTo(saved);
    // 130,000 + 80,000 + 30,000 + commercial fees 15,000 + 30,000 = 285,000; × 1.16 = 330,600.
    Job.Repricing preview = jobs.previewReprice(saved.id());
    assertThat(preview.afterQuote().grandTotal()).isEqualTo(Money.of(330_600));

    jobs.applyReprice(saved.id(), Money.of(330_600));
    assertThat(lastAudit().details())
        .isEqualTo("grand total: KSh 298,120.00 → KSh 330,600.00; category: Domestic → Commercial");
  }

  @Test
  void editAndRepriceAreRefusedOnceApproved() {
    Job saved = createExample2();
    jobs.advance(saved.id(), JobStatus.APPROVED);

    assertThatThrownBy(() -> jobs.editQuote(saved.id(), f.basicDomestic("90"), ""))
        .isInstanceOf(QuoteLockedException.class);
    assertThatThrownBy(() -> jobs.previewReprice(saved.id()))
        .isInstanceOf(QuoteLockedException.class);
    assertThatThrownBy(() -> jobs.applyReprice(saved.id(), saved.quote().grandTotal()))
        .isInstanceOf(QuoteLockedException.class);
  }

  // --- Lifecycle -------------------------------------------------------------------------------

  @Test
  void fullLifecycleRecordsHistoryInOrderAndAudits() {
    Job job = createExample2();
    for (JobStatus next : List.of(JobStatus.APPROVED, JobStatus.IN_PROGRESS, JobStatus.COMPLETED)) {
      f.ctx.clock.advance(Duration.ofHours(1));
      jobs.advance(job.id(), next);
    }

    assertThat(reload(job).status()).isEqualTo(JobStatus.COMPLETED);
    assertThat(jobs.history(job.id()))
        .extracting(StatusHistoryEntry::from, StatusHistoryEntry::to)
        .containsExactly(
            tuple(Optional.empty(), JobStatus.PENDING_SURVEY),
            tuple(Optional.of(JobStatus.PENDING_SURVEY), JobStatus.APPROVED),
            tuple(Optional.of(JobStatus.APPROVED), JobStatus.IN_PROGRESS),
            tuple(Optional.of(JobStatus.IN_PROGRESS), JobStatus.COMPLETED));
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_STATUS_CHANGED);
    assertThat(lastAudit().details()).isEqualTo("status: In progress → Completed");
  }

  @Test
  void skippingAStatusIsRefused() {
    Job job = createExample2();
    assertThatThrownBy(() -> jobs.advance(job.id(), JobStatus.IN_PROGRESS))
        .isInstanceOf(IllegalStatusTransitionException.class);
    assertThat(reload(job).status()).isEqualTo(JobStatus.PENDING_SURVEY);
  }

  @Test
  void advanceCannotBeUsedToCancel() {
    Job job = createExample2();
    f.signInAs(Role.ADMIN);
    assertThatThrownBy(() -> jobs.advance(job.id(), JobStatus.CANCELLED))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("Cancel job");
  }

  @Test
  void cancelRequiresAReasonAndRecordsIt() {
    Job job = createExample2();
    f.signInAs(Role.MANAGER);

    assertThatThrownBy(() -> jobs.cancel(job.id(), "  ")).isInstanceOf(ValidationException.class);
    jobs.cancel(job.id(), "Client withdrew");

    assertThat(jobs.history(job.id()).getLast().reason()).isEqualTo("Client withdrew");
    assertThat(lastAudit().action()).isEqualTo(AuditAction.JOB_CANCELLED);
    assertThat(lastAudit().details())
        .isEqualTo("status: Pending survey → Cancelled; reason: Client withdrew");
  }

  @Test
  void notesCanChangeAfterTheQuoteIsLocked() {
    Job job = createExample2();
    jobs.advance(job.id(), JobStatus.APPROVED);

    jobs.updateNotes(job.id(), "Rig arrives Monday");

    assertThat(reload(job).notes()).isEqualTo("Rig arrives Monday");
    assertThat(lastAudit().details()).isEqualTo("notes: Near the gate → Rig arrives Monday");
    int entries = f.ctx.auditEntries().size();
    jobs.updateNotes(job.id(), "Rig arrives Monday");
    assertThat(f.ctx.auditEntries()).hasSize(entries);
  }

  @Test
  void searchAndSummaryExposeDerivedAccount() {
    Job job = createExample2();
    JobSummary summary = jobs.summary(job.id()).orElseThrow();
    assertThat(summary.balance()).isEqualTo(Money.of(298_120));
    assertThat(jobs.search(JobSearch.all().withClient(client.id()))).hasSize(1);
  }

  // --- Rollback --------------------------------------------------------------------------------

  @Test
  void failurePartWayThroughCreatingAJobRollsEverythingBack() {
    JobRepository failsAfterInsert =
        new DelegatingJobRepository(f.jobRepository) {
          @Override
          public Job insert(Job job) {
            super.insert(job); // job row, lines, tier snapshot and history are now written
            throw new IllegalStateException("simulated crash before the job is audited");
          }
        };
    JobService failing = f.jobService(failsAfterInsert);

    assertThatThrownBy(() -> failing.create(client.id(), f.basicDomestic("80"), ""))
        .hasMessageContaining("simulated crash");

    for (String table :
        List.of("jobs", "job_lines", "plumbing_lines", "job_rate_tiers", "job_status_history")) {
      assertThat(f.count(table)).as(table).isZero();
    }
    assertThat(f.ctx.auditEntries()).noneMatch(e -> e.action() == AuditAction.JOB_CREATED);
    // The job number taken by the failed transaction is handed out again: no gap.
    assertThat(createExample2().number().value()).isEqualTo("JOB-2026-00001");
  }
}
