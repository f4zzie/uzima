package com.uzima.persistence;

import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.PipeRun;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.ReceiptNumber;
import com.uzima.pricing.PricingEngine;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A migrated database plus the job and payment repositories, with helpers that build realistic jobs
 * from the V2 reference data (looked up by name, never by assumed id).
 */
final class JobFixtures implements AutoCloseable {

  static final Instant CREATED = Instant.parse("2026-10-01T07:00:00Z");

  final Database db;
  final JdbcTransactions tx;
  final Jdbc jdbc;
  final JdbcJobRepository jobs;
  final JdbcPaymentRepository payments;
  final PricingEngine engine = new PricingEngine();
  final long userId;

  private final AtomicInteger jobSeq = new AtomicInteger();
  private final AtomicInteger receiptSeq = new AtomicInteger();
  private final AtomicInteger clientSeq = new AtomicInteger();

  JobFixtures(TestDatabases.Kind kind) {
    db = TestDatabases.open(kind);
    tx = new JdbcTransactions(db.dataSource());
    jdbc = new Jdbc(tx);
    jobs = new JdbcJobRepository(tx, jdbc);
    payments =
        new JdbcPaymentRepository(
            tx, jdbc, Clock.fixed(Instant.parse("2026-10-05T08:00:00Z"), ZoneOffset.UTC));
    userId = TestDatabases.insertUser(db, "clerk", "CLERK");
  }

  long client(String name, ClientCategory category) {
    String code = "UZM-%s-%04d".formatted(category.code(), clientSeq.incrementAndGet());
    return jdbc.insert(
        "INSERT INTO clients (code, name, category, phone, county, created_at, created_by)"
            + " VALUES (?, ?, ?, '+254712345678', 'NAIROBI', ?, ?)",
        code,
        name,
        category,
        CREATED,
        userId);
  }

  DrillingType drilling(String name) {
    return jdbc.queryOne(
            "SELECT id, name, down_payment FROM drilling_types WHERE name = ?",
            rs ->
                new DrillingType(
                    rs.getLong(1), rs.getString(2), Jdbc.money(rs, "down_payment"), true),
            name)
        .orElseThrow();
  }

  PumpType pump(String name) {
    return jdbc.queryOne(
            "SELECT id, name, base_cost FROM pump_types WHERE name = ?",
            rs -> new PumpType(rs.getLong(1), rs.getString(2), Jdbc.money(rs, "base_cost"), true),
            name)
        .orElseThrow();
  }

  TankType tank(int litres) {
    return jdbc.queryOne(
            "SELECT id, capacity_litres, price FROM tank_types WHERE capacity_litres = ?",
            rs -> new TankType(rs.getLong(1), rs.getInt(2), Jdbc.money(rs, "price"), true),
            litres)
        .orElseThrow();
  }

  PipeType pipe(String name) {
    return jdbc.queryOne(
            "SELECT id, name, cost_per_metre FROM pipe_types WHERE name = ?",
            rs ->
                new PipeType(
                    rs.getLong(1), rs.getString(2), Jdbc.money(rs, "cost_per_metre"), true),
            name)
        .orElseThrow();
  }

  /** The live fees and tiers for {@code category} at 16% VAT. */
  PriceBasis basis(ClientCategory category) {
    CategoryFees fees =
        jdbc.queryOne(
                "SELECT survey_fee, authority_fee FROM category_fees WHERE category = ?",
                rs ->
                    new CategoryFees(
                        category, Jdbc.money(rs, "survey_fee"), Jdbc.money(rs, "authority_fee")),
                category)
            .orElseThrow();
    List<RateTier> tiers =
        jdbc.query(
            "SELECT from_metres, to_metres, rate_per_metre FROM rate_tiers ORDER BY position",
            rs ->
                new RateTier(
                    Jdbc.metres(rs, "from_metres"),
                    Optional.ofNullable(Jdbc.metres(rs, "to_metres")),
                    Jdbc.money(rs, "rate_per_metre")));
    return new PriceBasis(fees, new RateTable(tiers), TaxRate.KENYA_STANDARD);
  }

  /** Acceptance example 1: industrial, full service with manual plumbing. Total 684,400.00. */
  QuoteSpec fullService() {
    return new QuoteSpec(
        Optional.of(new DrillingSpec(drilling("Symmetric"), Metres.of(150))),
        Optional.of(new PumpSpec(pump("Submersible electric"), Metres.of(10))),
        Optional.of(tank(5000)),
        Optional.of(new Plumbing.Manual(Money.of(50_000), "Manual plumbing")));
  }

  QuoteSpec itemisedPlumbing() {
    return new QuoteSpec(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.of(
            new Plumbing.Itemised(
                List.of(
                    new PipeRun(pipe("PVC"), 25, Metres.of("30")),
                    new PipeRun(pipe("HDPE"), 50, Metres.of("12.5")),
                    new PipeRun(pipe("Galvanized"), 25, Metres.of("4.25"))),
                3)));
  }

  QuoteSpec tankOnly() {
    return new QuoteSpec(
        Optional.empty(), Optional.empty(), Optional.of(tank(1000)), Optional.empty());
  }

  Job newJob(long clientId, ClientCategory category, QuoteSpec spec, Instant createdAt) {
    PriceBasis basis = basis(category);
    return Job.create(
        new JobNumber(Year.of(2026), jobSeq.incrementAndGet()),
        clientId,
        spec,
        engine.quote(spec, basis),
        basis,
        "Site near the gate",
        createdAt,
        userId);
  }

  Job save(Job job) {
    return tx.inTransaction(() -> jobs.insert(job));
  }

  Payment.Received pay(Job job, long shillings, LocalDate date) {
    return (Payment.Received)
        tx.inTransaction(
            () ->
                payments.insert(
                    new Payment.Received(
                        0,
                        receipt(),
                        job.id(),
                        Money.of(shillings),
                        date,
                        PaymentMethod.MPESA,
                        "QJK3H5L7P" + (receiptSeq.get() % 10),
                        userId,
                        "")));
  }

  Payment.Reversal reverse(Payment.Received original, long shillings, LocalDate date) {
    return (Payment.Reversal)
        tx.inTransaction(
            () ->
                payments.insert(
                    new Payment.Reversal(
                        0,
                        receipt(),
                        original.jobId(),
                        Money.of(shillings).negate(),
                        date,
                        PaymentMethod.CASH,
                        "",
                        userId,
                        "",
                        original.id(),
                        "Refund")));
  }

  ReceiptNumber receipt() {
    return new ReceiptNumber(Year.of(2026), receiptSeq.incrementAndGet());
  }

  int count(String table) {
    return jdbc.queryOne("SELECT COUNT(*) FROM " + table, rs -> rs.getInt(1)).orElseThrow();
  }

  @Override
  public void close() {
    db.close();
  }
}
