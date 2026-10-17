package com.uzima.app;

import com.uzima.domain.common.DisplayFormats;
import com.uzima.persistence.Database;
import com.uzima.persistence.Jdbc;
import com.uzima.persistence.JdbcAuditRepository;
import com.uzima.persistence.JdbcCatalogRepository;
import com.uzima.persistence.JdbcClientRepository;
import com.uzima.persistence.JdbcJobRepository;
import com.uzima.persistence.JdbcPaymentRepository;
import com.uzima.persistence.JdbcSequenceGenerator;
import com.uzima.persistence.JdbcSettingsRepository;
import com.uzima.persistence.JdbcTransactions;
import com.uzima.persistence.JdbcUserRepository;
import com.uzima.pricing.PricingEngine;
import com.uzima.service.AuditLog;
import com.uzima.service.AuthService;
import com.uzima.service.BcryptPasswordHasher;
import com.uzima.service.CatalogService;
import com.uzima.service.ClientService;
import com.uzima.service.JobService;
import com.uzima.service.PasswordHasher;
import com.uzima.service.PaymentService;
import com.uzima.service.Session;
import com.uzima.service.SettingsService;
import com.uzima.service.TemporaryPasswords;
import com.uzima.service.UserService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Clock;

/**
 * Builds the object graph by hand. Every long-lived object is created here and passed down through
 * constructors; nothing else in the app uses static singletons.
 */
public final class CompositionRoot implements AutoCloseable {

  private final AppConfig config;
  private final Database database;
  private final String databaseDescription;
  private final Services services;

  private CompositionRoot(AppConfig config, Database database, String databaseDescription) {
    this.config = config;
    this.database = database;
    this.databaseDescription = databaseDescription;
    this.services = wire(database, Clock.system(DisplayFormats.ZONE));
  }

  /** Builds repositories and services. Package-private so tests can wire against any database. */
  static Services wire(Database database, Clock clock) {
    JdbcTransactions tx = new JdbcTransactions(database.dataSource());
    Jdbc jdbc = new Jdbc(tx);

    var userRepository = new JdbcUserRepository(jdbc);
    var settingsRepository = new JdbcSettingsRepository(jdbc);
    var clientRepository = new JdbcClientRepository(jdbc);
    var catalogRepository = new JdbcCatalogRepository(jdbc);
    var jobRepository = new JdbcJobRepository(tx, jdbc);
    var paymentRepository = new JdbcPaymentRepository(tx, jdbc, clock);
    var sequences = new JdbcSequenceGenerator(tx, jdbc);

    Session session = new Session();
    AuditLog audit = new AuditLog(new JdbcAuditRepository(jdbc), session, clock);
    PasswordHasher hasher = new BcryptPasswordHasher();
    PricingEngine pricing = new PricingEngine();

    SettingsService settings = new SettingsService(settingsRepository, session, audit, tx);
    CatalogService catalog = new CatalogService(catalogRepository, tx, session, audit);
    ClientService clients =
        new ClientService(clientRepository, sequences, tx, session, audit, clock);
    JobService jobs =
        new JobService(
            tx,
            session,
            audit,
            clock,
            sequences,
            jobRepository,
            clientRepository,
            catalog,
            settings,
            pricing);
    PaymentService payments =
        new PaymentService(tx, session, audit, clock, sequences, jobRepository, paymentRepository);

    return new Services(
        session,
        new AuthService(userRepository, hasher, session, audit, tx, clock),
        new UserService(
            userRepository, hasher, new TemporaryPasswords(), session, audit, tx, clock),
        settings,
        clients,
        catalog,
        jobs,
        payments,
        audit,
        pricing);
  }

  /** Opens and migrates the database. Runs off the JavaFX thread. */
  public static CompositionRoot start(AppConfig config) {
    try {
      Files.createDirectories(config.dataDir());
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot create app data directory " + config.dataDir(), e);
    }
    Database database = Database.open(config.database());
    try {
      database.migrate();
      return new CompositionRoot(config, database, database.describe());
    } catch (RuntimeException e) {
      database.close();
      throw e;
    }
  }

  public AppConfig config() {
    return config;
  }

  public Services services() {
    return services;
  }

  public String databaseDescription() {
    return databaseDescription;
  }

  @Override
  public void close() {
    database.close();
  }
}
