package com.uzima.persistence;

import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.PipeRun;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuantityUnit;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteLine;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.job.StatusChange;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.service.JobRepository;
import com.uzima.service.JobSearch;
import com.uzima.service.JobSummary;
import com.uzima.service.StatusHistoryEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Jobs and their snapshot tables ({@code job_lines}, {@code plumbing_lines}, {@code
 * job_rate_tiers}, {@code job_status_history}). Catalog items read back from a job are its snapshot
 * copies, so they are always marked active: whether the live item was later retired is irrelevant
 * to what the job was quoted at.
 */
public final class JdbcJobRepository implements JobRepository {

  /** Snapshot columns written on insert and on {@link #replaceQuote}, in bind order. */
  private static final List<String> SNAPSHOT_COLUMNS =
      List.of(
          "drilling_type_id",
          "drilling_type_name",
          "drilling_down_payment",
          "depth_metres",
          "pump_type_id",
          "pump_type_name",
          "pump_base_cost",
          "lift_height_metres",
          "tank_type_id",
          "tank_capacity_litres",
          "tank_price",
          "plumbing_mode",
          "plumbing_outlets",
          "plumbing_manual_amount",
          "plumbing_manual_description",
          "priced_category",
          "survey_fee",
          "authority_fee",
          "tax_rate",
          "subtotal",
          "tax_amount",
          "grand_total",
          "notes");

  private static final String ITEMISED = "ITEMISED";
  private static final String MANUAL = "MANUAL";

  private static final String SUMMARY_SELECT =
      "SELECT j.id, j.job_number, j.client_id, c.code AS client_code, c.name AS client_name,"
          + " c.category AS client_category, j.status, a.grand_total, a.amount_paid, a.balance,"
          + " a.payment_status, a.refund_due, j.created_at"
          + " FROM jobs j"
          + " JOIN job_accounts a ON a.job_id = j.id"
          + " JOIN clients c ON c.id = j.client_id";

  private final JdbcTransactions tx;
  private final Jdbc jdbc;

  public JdbcJobRepository(JdbcTransactions tx, Jdbc jdbc) {
    this.tx = tx;
    this.jdbc = jdbc;
  }

  // --- Writes ---------------------------------------------------------------------------------

  @Override
  public Job insert(Job job) {
    requireTransaction();
    if (job.id() != 0) {
      throw new IllegalArgumentException("Job is already saved: " + job.id());
    }
    List<Object> params = new ArrayList<>();
    params.add(job.number().value());
    params.add(job.clientId());
    params.add(job.status());
    params.addAll(snapshotValues(job));
    params.add(job.createdAt());
    params.add(job.createdBy());
    params.add(job.createdAt());
    long id =
        jdbc.insert(
            "INSERT INTO jobs (job_number, client_id, status, "
                + String.join(", ", SNAPSHOT_COLUMNS)
                + ", created_at, created_by, updated_at) VALUES ("
                + placeholders(3 + SNAPSHOT_COLUMNS.size() + 3)
                + ")",
            params.toArray());
    writeChildren(id, job);
    jdbc.update(
        "INSERT INTO job_status_history (job_id, from_status, to_status, reason, changed_at,"
            + " changed_by) VALUES (?, NULL, ?, '', ?, ?)",
        id,
        job.status(),
        job.createdAt(),
        job.createdBy());
    return new Job(
        id,
        job.number(),
        job.clientId(),
        job.status(),
        job.spec(),
        job.quote(),
        job.priceBasis(),
        job.notes(),
        job.createdAt(),
        job.createdBy());
  }

  @Override
  public void replaceQuote(Job job, Instant at) {
    requireTransaction();
    List<Object> params = new ArrayList<>(snapshotValues(job));
    params.add(at);
    params.add(job.id());
    params.add(JobStatus.PENDING_SURVEY);
    int updated =
        jdbc.update(
            "UPDATE jobs SET "
                + String.join(" = ?, ", SNAPSHOT_COLUMNS)
                + " = ?, updated_at = ? WHERE id = ? AND status = ?",
            params.toArray());
    if (updated != 1) {
      // The domain only allows quote changes while awaiting survey; this is the last line of
      // defence against a stale screen saving over a job someone else has moved on.
      throw new IllegalStateException(
          "Job " + job.number() + " is missing or no longer awaiting survey");
    }
    for (String table : List.of("job_lines", "plumbing_lines", "job_rate_tiers")) {
      jdbc.update("DELETE FROM " + table + " WHERE job_id = ?", job.id());
    }
    writeChildren(job.id(), job);
  }

  @Override
  public void recordStatusChange(long jobId, StatusChange change) {
    requireTransaction();
    int updated =
        jdbc.update(
            "UPDATE jobs SET status = ?, updated_at = ? WHERE id = ? AND status = ?",
            change.to(),
            change.at(),
            jobId,
            change.from());
    if (updated != 1) {
      throw new IllegalStateException(
          "Job " + jobId + " is missing or is no longer " + change.from().label());
    }
    jdbc.update(
        "INSERT INTO job_status_history (job_id, from_status, to_status, reason, changed_at,"
            + " changed_by) VALUES (?, ?, ?, ?, ?, ?)",
        jobId,
        change.from(),
        change.to(),
        change.reason(),
        change.at(),
        change.changedBy());
  }

  @Override
  public void updateNotes(long jobId, String notes, Instant at) {
    int updated =
        jdbc.update(
            "UPDATE jobs SET notes = ?, updated_at = ? WHERE id = ?",
            notes == null ? "" : notes.strip(),
            at,
            jobId);
    if (updated != 1) {
      throw new IllegalStateException("No job with id " + jobId);
    }
  }

  private void writeChildren(long jobId, Job job) {
    List<RateTier> tiers = job.priceBasis().rates().tiers();
    for (int i = 0; i < tiers.size(); i++) {
      RateTier t = tiers.get(i);
      jdbc.update(
          "INSERT INTO job_rate_tiers (job_id, position, from_metres, to_metres, rate_per_metre)"
              + " VALUES (?, ?, ?, ?, ?)",
          jobId,
          i + 1,
          t.from(),
          t.to().orElse(null),
          t.ratePerMetre());
    }
    List<QuoteLine> lines = job.quote().lines();
    for (int i = 0; i < lines.size(); i++) {
      QuoteLine l = lines.get(i);
      jdbc.update(
          "INSERT INTO job_lines (job_id, position, category, description, quantity, unit,"
              + " unit_price, line_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          jobId,
          i + 1,
          l.category(),
          l.description(),
          l.quantity(),
          l.unit(),
          l.unitPrice(),
          l.lineTotal());
    }
    if (job.spec().plumbing().orElse(null) instanceof Plumbing.Itemised itemised) {
      List<PipeRun> runs = itemised.runs();
      for (int i = 0; i < runs.size(); i++) {
        PipeRun r = runs.get(i);
        jdbc.update(
            "INSERT INTO plumbing_lines (job_id, position, pipe_type_id, pipe_name,"
                + " cost_per_metre, diameter_mm, length_metres) VALUES (?, ?, ?, ?, ?, ?, ?)",
            jobId,
            i + 1,
            r.pipe().id(),
            r.pipe().name(),
            r.pipe().costPerMetre(),
            r.diameterMm(),
            r.length());
      }
    }
  }

  private static List<Object> snapshotValues(Job job) {
    QuoteSpec spec = job.spec();
    List<Object> v = new ArrayList<>();
    Optional<DrillingSpec> drilling = spec.drilling();
    v.add(drilling.map(d -> d.type().id()).orElse(null));
    v.add(drilling.map(d -> d.type().name()).orElse(null));
    v.add(drilling.map(d -> d.type().downPayment()).orElse(null));
    v.add(drilling.map(DrillingSpec::depth).orElse(null));
    Optional<PumpSpec> pump = spec.pump();
    v.add(pump.map(p -> p.type().id()).orElse(null));
    v.add(pump.map(p -> p.type().name()).orElse(null));
    v.add(pump.map(p -> p.type().baseCost()).orElse(null));
    v.add(pump.map(PumpSpec::liftHeight).orElse(null));
    Optional<TankType> tank = spec.tank();
    v.add(tank.map(TankType::id).orElse(null));
    v.add(tank.map(TankType::capacityLitres).orElse(null));
    v.add(tank.map(TankType::price).orElse(null));
    switch (spec.plumbing().orElse(null)) {
      case null -> {
        v.add(null);
        v.add(null);
        v.add(null);
        v.add(null);
      }
      case Plumbing.Itemised itemised -> {
        v.add(ITEMISED);
        v.add(itemised.outlets());
        v.add(null);
        v.add(null);
      }
      case Plumbing.Manual manual -> {
        v.add(MANUAL);
        v.add(null);
        v.add(manual.amount());
        v.add(manual.description());
      }
    }
    PriceBasis basis = job.priceBasis();
    v.add(basis.fees().category());
    v.add(basis.fees().surveyFee());
    v.add(basis.fees().authorityFee());
    v.add(basis.taxRate().value());
    Quote quote = job.quote();
    v.add(quote.subtotal());
    v.add(quote.tax());
    v.add(quote.grandTotal());
    v.add(job.notes());
    return v;
  }

  // --- Reads ----------------------------------------------------------------------------------

  @Override
  public Optional<Job> findById(long id) {
    return jdbc.queryOne("SELECT * FROM jobs WHERE id = ?", JobRow::read, id).map(this::assemble);
  }

  @Override
  public Optional<Job> findByNumber(JobNumber number) {
    return jdbc.queryOne("SELECT * FROM jobs WHERE job_number = ?", JobRow::read, number.value())
        .map(this::assemble);
  }

  @Override
  public List<StatusHistoryEntry> history(long jobId) {
    return jdbc.query(
        "SELECT from_status, to_status, reason, changed_at, changed_by FROM job_status_history"
            + " WHERE job_id = ? ORDER BY changed_at, id",
        rs ->
            new StatusHistoryEntry(
                Optional.ofNullable(Jdbc.enumValue(rs, "from_status", JobStatus.class)),
                Jdbc.enumValue(rs, "to_status", JobStatus.class),
                rs.getString("reason"),
                Jdbc.instant(rs, "changed_at"),
                rs.getLong("changed_by")),
        jobId);
  }

  @Override
  public List<JobSummary> search(JobSearch s) {
    StringBuilder sql = new StringBuilder(SUMMARY_SELECT).append(" WHERE 1 = 1");
    List<Object> params = new ArrayList<>();
    s.status().ifPresent(v -> where(sql, params, " AND j.status = ?", v));
    s.paymentStatus().ifPresent(v -> where(sql, params, " AND a.payment_status = ?", v));
    s.createdFrom().ifPresent(d -> where(sql, params, " AND j.created_at >= ?", startOf(d)));
    s.createdTo()
        .ifPresent(d -> where(sql, params, " AND j.created_at < ?", startOf(d.plusDays(1))));
    s.clientId().ifPresent(v -> where(sql, params, " AND j.client_id = ?", v));
    s.clientCategory().ifPresent(v -> where(sql, params, " AND c.category = ?", v));
    s.text()
        .ifPresent(
            t -> {
              String like = "%" + JdbcAuditRepository.escapeLike(t.toLowerCase()) + "%";
              sql.append(
                  " AND (LOWER(j.job_number) LIKE ? ESCAPE '!' OR LOWER(c.name) LIKE ? ESCAPE '!'"
                      + " OR LOWER(c.code) LIKE ? ESCAPE '!')");
              params.add(like);
              params.add(like);
              params.add(like);
            });
    sql.append(" ORDER BY j.created_at DESC, j.id DESC LIMIT ?");
    params.add(s.limit());
    return jdbc.query(sql.toString(), JdbcJobRepository::summary, params.toArray());
  }

  @Override
  public Optional<JobSummary> summary(long jobId) {
    return jdbc.queryOne(SUMMARY_SELECT + " WHERE j.id = ?", JdbcJobRepository::summary, jobId);
  }

  private Job assemble(JobRow row) {
    List<RateTier> tiers =
        jdbc.query(
            "SELECT from_metres, to_metres, rate_per_metre FROM job_rate_tiers"
                + " WHERE job_id = ? ORDER BY position",
            rs ->
                new RateTier(
                    Jdbc.metres(rs, "from_metres"),
                    Optional.ofNullable(Jdbc.metres(rs, "to_metres")),
                    Jdbc.money(rs, "rate_per_metre")),
            row.id);
    List<QuoteLine> lines =
        jdbc.query(
            "SELECT category, description, quantity, unit, unit_price, line_total FROM job_lines"
                + " WHERE job_id = ? ORDER BY position",
            rs ->
                new QuoteLine(
                    Jdbc.enumValue(rs, "category", LineCategory.class),
                    rs.getString("description"),
                    rs.getBigDecimal("quantity"),
                    Jdbc.enumValue(rs, "unit", QuantityUnit.class),
                    Jdbc.money(rs, "unit_price"),
                    Jdbc.money(rs, "line_total")),
            row.id);

    Optional<Plumbing> plumbing =
        switch (row.plumbingMode) {
          case null -> Optional.empty();
          case ITEMISED -> Optional.of(new Plumbing.Itemised(pipeRuns(row.id), row.outlets));
          case MANUAL -> Optional.of(new Plumbing.Manual(row.manualAmount, row.manualDescription));
          default -> throw new IllegalStateException("Unknown plumbing mode " + row.plumbingMode);
        };
    QuoteSpec spec =
        new QuoteSpec(
            Optional.ofNullable(row.drilling),
            Optional.ofNullable(row.pump),
            Optional.ofNullable(row.tank),
            plumbing);
    PriceBasis basis = new PriceBasis(row.fees, new RateTable(tiers), row.taxRate);
    Quote quote = new Quote(lines, row.subtotal, row.taxRate, row.tax, row.grandTotal);
    return new Job(
        row.id,
        row.number,
        row.clientId,
        row.status,
        spec,
        quote,
        basis,
        row.notes,
        row.createdAt,
        row.createdBy);
  }

  private List<PipeRun> pipeRuns(long jobId) {
    return jdbc.query(
        "SELECT pipe_type_id, pipe_name, cost_per_metre, diameter_mm, length_metres"
            + " FROM plumbing_lines WHERE job_id = ? ORDER BY position",
        rs ->
            new PipeRun(
                new PipeType(
                    rs.getLong("pipe_type_id"),
                    rs.getString("pipe_name"),
                    Jdbc.money(rs, "cost_per_metre"),
                    true),
                rs.getInt("diameter_mm"),
                Jdbc.metres(rs, "length_metres")),
        jobId);
  }

  private static JobSummary summary(ResultSet rs) throws SQLException {
    return new JobSummary(
        rs.getLong("id"),
        JobNumber.parse(rs.getString("job_number")),
        rs.getLong("client_id"),
        ClientCode.parse(rs.getString("client_code")),
        rs.getString("client_name"),
        Jdbc.enumValue(rs, "client_category", ClientCategory.class),
        Jdbc.enumValue(rs, "status", JobStatus.class),
        Jdbc.money(rs, "grand_total"),
        Jdbc.money(rs, "amount_paid"),
        Jdbc.money(rs, "balance"),
        Jdbc.enumValue(rs, "payment_status", PaymentStatus.class),
        Jdbc.money(rs, "refund_due"),
        Jdbc.instant(rs, "created_at"));
  }

  /** The jobs row, read before the child tables are queried on the same connection. */
  private record JobRow(
      long id,
      JobNumber number,
      long clientId,
      JobStatus status,
      DrillingSpec drilling,
      PumpSpec pump,
      TankType tank,
      String plumbingMode,
      int outlets,
      Money manualAmount,
      String manualDescription,
      CategoryFees fees,
      TaxRate taxRate,
      Money subtotal,
      Money tax,
      Money grandTotal,
      String notes,
      Instant createdAt,
      long createdBy) {

    static JobRow read(ResultSet rs) throws SQLException {
      Long drillingId = Jdbc.nullableLong(rs, "drilling_type_id");
      DrillingSpec drilling =
          drillingId == null
              ? null
              : new DrillingSpec(
                  new DrillingType(
                      drillingId,
                      rs.getString("drilling_type_name"),
                      Jdbc.money(rs, "drilling_down_payment"),
                      true),
                  Jdbc.metres(rs, "depth_metres"));
      Long pumpId = Jdbc.nullableLong(rs, "pump_type_id");
      PumpSpec pump =
          pumpId == null
              ? null
              : new PumpSpec(
                  new PumpType(
                      pumpId,
                      rs.getString("pump_type_name"),
                      Jdbc.money(rs, "pump_base_cost"),
                      true),
                  Jdbc.metres(rs, "lift_height_metres"));
      Long tankId = Jdbc.nullableLong(rs, "tank_type_id");
      TankType tank =
          tankId == null
              ? null
              : new TankType(
                  tankId, rs.getInt("tank_capacity_litres"), Jdbc.money(rs, "tank_price"), true);
      ClientCategory pricedCategory = Jdbc.enumValue(rs, "priced_category", ClientCategory.class);
      return new JobRow(
          rs.getLong("id"),
          JobNumber.parse(rs.getString("job_number")),
          rs.getLong("client_id"),
          Jdbc.enumValue(rs, "status", JobStatus.class),
          drilling,
          pump,
          tank,
          rs.getString("plumbing_mode"),
          rs.getInt("plumbing_outlets"),
          Jdbc.money(rs, "plumbing_manual_amount"),
          rs.getString("plumbing_manual_description"),
          new CategoryFees(
              pricedCategory, Jdbc.money(rs, "survey_fee"), Jdbc.money(rs, "authority_fee")),
          new TaxRate(rs.getBigDecimal("tax_rate")),
          Jdbc.money(rs, "subtotal"),
          Jdbc.money(rs, "tax_amount"),
          Jdbc.money(rs, "grand_total"),
          rs.getString("notes"),
          Jdbc.instant(rs, "created_at"),
          rs.getLong("created_by"));
    }
  }

  // --- Helpers --------------------------------------------------------------------------------

  private void requireTransaction() {
    if (!tx.inTransaction()) {
      throw new IllegalStateException("Job writes must run inside a transaction");
    }
  }

  private static void where(StringBuilder sql, List<Object> params, String clause, Object value) {
    sql.append(clause);
    params.add(value);
  }

  /** Start of {@code day} in Nairobi, as the UTC instant stored in the database. */
  private static Instant startOf(LocalDate day) {
    return day.atStartOfDay(DisplayFormats.ZONE).toInstant();
  }

  private static String placeholders(int count) {
    return String.join(", ", Collections.nCopies(count, "?"));
  }
}
