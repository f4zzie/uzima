package com.uzima.service;

import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.persistence.JdbcCatalogRepository;
import com.uzima.persistence.JdbcClientRepository;
import com.uzima.persistence.JdbcJobRepository;
import com.uzima.persistence.JdbcSettingsRepository;
import com.uzima.persistence.TestDatabases;
import com.uzima.pricing.PricingEngine;
import java.util.Optional;

/** Real services on a fresh database, for job service tests. */
final class JobServiceFixture implements AutoCloseable {

  final ServiceTestContext ctx;
  final ClientService clients;
  final CatalogService catalog;
  final SettingsService settings;
  final JdbcJobRepository jobRepository;
  final JobService jobs;

  JobServiceFixture(ServiceTestContext ctx) {
    this.ctx = ctx;
    this.clients =
        new ClientService(
            new JdbcClientRepository(ctx.jdbc),
            ctx.sequences,
            ctx.tx,
            ctx.session,
            ctx.audit,
            ctx.clock);
    this.catalog =
        new CatalogService(new JdbcCatalogRepository(ctx.jdbc), ctx.tx, ctx.session, ctx.audit);
    this.settings =
        new SettingsService(new JdbcSettingsRepository(ctx.jdbc), ctx.session, ctx.audit, ctx.tx);
    this.jobRepository = new JdbcJobRepository(ctx.tx, ctx.jdbc);
    this.jobs = jobService(jobRepository);
  }

  static JobServiceFixture h2() {
    return new JobServiceFixture(ServiceTestContext.h2());
  }

  static JobServiceFixture open(TestDatabases.Kind kind) {
    return new JobServiceFixture(ServiceTestContext.open(kind));
  }

  JobService jobService(JobRepository repository) {
    return new JobService(
        ctx.tx,
        ctx.session,
        ctx.audit,
        ctx.clock,
        ctx.sequences,
        repository,
        new JdbcClientRepository(ctx.jdbc),
        catalog,
        settings,
        new PricingEngine());
  }

  User signInAs(Role role) {
    return ctx.signInAs(role);
  }

  Client client(ClientCategory category) {
    return clients.register(ClientServiceTest.details("Client " + category, category));
  }

  DrillingType drilling(String name) {
    return catalog.activeDrillingTypes().stream()
        .filter(d -> d.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  PumpType pump(String name) {
    return catalog.activePumpTypes().stream()
        .filter(p -> p.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  /** Acceptance example 2: Symmetric drilling at {@code depth} m and a hand pump at 0 m. */
  QuoteSpec basicDomestic(String depth) {
    return new QuoteSpec(
        Optional.of(new DrillingSpec(drilling("Symmetric"), Metres.of(depth))),
        Optional.of(new PumpSpec(pump("Hand"), Metres.ZERO)),
        Optional.empty(),
        Optional.empty());
  }

  void setTaxRate(String fraction) {
    Settings s = settings.current();
    settings.update(
        new Settings(
            s.companyName(),
            s.address(),
            s.phone(),
            s.email(),
            s.kraPin(),
            s.logoPath(),
            TaxRate.of(fraction),
            s.idleLockMinutes(),
            s.exportFolder()));
  }

  int count(String table) {
    return ctx.jdbc.queryOne("SELECT COUNT(*) FROM " + table, rs -> rs.getInt(1)).orElseThrow();
  }

  @Override
  public void close() {
    ctx.close();
  }
}
