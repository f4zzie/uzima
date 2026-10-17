package com.uzima.service;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.persistence.JdbcCatalogRepository;
import com.uzima.persistence.JdbcJobRepository;
import com.uzima.persistence.JdbcPaymentRepository;
import com.uzima.persistence.TestDatabases;
import com.uzima.pricing.PricingEngine;
import java.time.Year;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A payment service on a fresh database, with helpers that create jobs directly through the job
 * repository (the job service is tested separately).
 */
final class PaymentTestFixture implements AutoCloseable {

  /** Domestic job with KSh 100,000 manual plumbing: 117,000 + 18,720 VAT = 135,720.00. */
  static final Money JOB_TOTAL = Money.of("135720.00");

  final ServiceTestContext ctx;
  final JdbcJobRepository jobs;
  final JdbcPaymentRepository payments;
  final JdbcCatalogRepository catalog;
  final PaymentService service;
  final User manager;
  private final PricingEngine engine = new PricingEngine();
  private final AtomicInteger clients = new AtomicInteger();

  PaymentTestFixture() {
    this(ServiceTestContext.h2());
  }

  PaymentTestFixture(TestDatabases.Kind kind) {
    this(ServiceTestContext.open(kind));
  }

  private PaymentTestFixture(ServiceTestContext ctx) {
    this.ctx = ctx;
    this.jobs = new JdbcJobRepository(ctx.tx, ctx.jdbc);
    this.payments = new JdbcPaymentRepository(ctx.tx, ctx.jdbc, ctx.clock);
    this.catalog = new JdbcCatalogRepository(ctx.jdbc);
    this.service = withAudit(ctx.audit);
    this.manager = ctx.signInAs(Role.MANAGER);
  }

  PaymentService withAudit(AuditLog audit) {
    return new PaymentService(ctx.tx, ctx.session, audit, ctx.clock, ctx.sequences, jobs, payments);
  }

  /** Saves a new PENDING_SURVEY domestic job totalling {@link #JOB_TOTAL}. */
  Job job() {
    long clientId =
        ctx.jdbc.insert(
            "INSERT INTO clients (code, name, category, phone, county, created_at, created_by)"
                + " VALUES (?, 'Achieng Otieno', 'DOMESTIC', '+254712345678', 'KISUMU', ?, ?)",
            "UZM-DOM-%04d".formatted(clients.incrementAndGet()),
            ServiceTestContext.START,
            manager.id());
    QuoteSpec spec =
        new QuoteSpec(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(new Plumbing.Manual(Money.of(100_000), "Plumbing to house")));
    PriceBasis basis =
        new PriceBasis(
            catalog.fees(ClientCategory.DOMESTIC), catalog.rateTable(), TaxRate.KENYA_STANDARD);
    return ctx.tx.inTransaction(
        () ->
            jobs.insert(
                Job.create(
                    ctx.sequences.nextJobNumber(Year.of(2026)),
                    clientId,
                    spec,
                    engine.quote(spec, basis),
                    basis,
                    "",
                    ServiceTestContext.START,
                    manager.id())));
  }

  /** Cancels a saved job the way the job service will: through the domain and the repository. */
  void cancel(Job job) {
    Job current = jobs.findById(job.id()).orElseThrow();
    if (current.status() == JobStatus.CANCELLED) {
      return;
    }
    var transition = current.cancel("Client withdrew", ctx.clock.instant(), manager.id());
    ctx.tx.run(() -> jobs.recordStatusChange(job.id(), transition.change()));
  }

  PricingEngine engine() {
    return engine;
  }

  @Override
  public void close() {
    ctx.close();
  }
}
