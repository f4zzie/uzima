package com.uzima.ui;

import com.uzima.app.Services;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.domain.common.DisplayFormats;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.service.ClientSearch;
import com.uzima.service.ClientSummary;
import com.uzima.service.JobSearch;
import com.uzima.service.JobSummary;
import com.uzima.service.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * One consistent set of clients, jobs and payments for every snapshot. Screens, dialogs, toasts and
 * the style guide all refer to these same records, because the snapshots become the README
 * screenshots. Seeded through the real services, so every amount comes from the pricing engine.
 *
 * <p>"Today" is Friday 16 October 2026 in Nairobi. The story:
 *
 * <ul>
 *   <li>JOB-2026-00001 Athi River Cement Ltd (UZM-IND-0001): acceptance example 1, KSh 684,400.00,
 *       in progress, KSh 84,400.00 paid by M-Pesa (RCP-2026-00001), KSh 600,000.00 to pay.
 *   <li>JOB-2026-00002 Wanjiru Kamau (UZM-DOM-0001): example 2, KSh 298,120.00, completed and paid
 *       in full (RCP-2026-00002 cash, RCP-2026-00004 M-Pesa).
 *   <li>JOB-2026-00003 Baraka Hotels Ltd (UZM-COM-0001): example 3, KSh 1,107,800.00, approved,
 *       nothing paid.
 *   <li>JOB-2026-00004 St. Mary's Girls School (UZM-COM-0002): cancelled after a KSh 200,000.00
 *       bank transfer (RCP-2026-00003), so KSh 200,000.00 is a refund due.
 *   <li>JOB-2026-00005 Otieno Ochieng (UZM-DOM-0002) and JOB-2026-00006 Chebet Kiprono
 *       (UZM-DOM-0003): pending survey.
 * </ul>
 */
public final class SampleData implements AutoCloseable {

  public static final LocalDate TODAY = LocalDate.of(2026, 10, 16);

  public final MutableClock clock = new MutableClock(at(2026, 9, 21, 8, 30), DisplayFormats.ZONE);
  public final UiTestBackend ui;
  public final Services services;

  public Client athiRiver;
  public Client wanjiru;
  public Client baraka;
  public Client stMarys;
  public Client otieno;
  public Client chebet;

  public Job athiRiverJob;
  public Job wanjiruJob;
  public Job barakaJob;
  public Job stMarysJob;
  public Job otienoJob;
  public Job chebetJob;

  public Payment.Received athiRiverDeposit;

  private SampleData() {
    ui = new UiTestBackend(clock);
    services = ui.services;
  }

  private static SampleData shared;

  /**
   * One seeded copy for the whole test run, for static specimens such as the style guide. Seed it
   * from a test's {@code @BeforeAll} (off the FX thread) before building any view that uses it.
   */
  public static synchronized SampleData shared() {
    if (shared == null) {
      shared = seed();
    }
    return shared;
  }

  /** Every job as the jobs list shows it, newest first. */
  public List<JobSummary> jobSummaries() {
    return services.jobs().search(JobSearch.all());
  }

  public List<ClientSummary> clientSummaries() {
    return services.clients().search(ClientSearch.all());
  }

  /**
   * Seeds the data and leaves the administrator, Amina Odhiambo, signed in. Call off the FX thread.
   */
  public static SampleData seed() {
    SampleData data = new SampleData();
    data.ui.signedInAsAdministrator();
    data.seedClients();
    data.seedJobs();
    data.clock.set(at(2026, 10, 16, 9, 30));
    return data;
  }

  private void seedClients() {
    athiRiver =
        client(
            "Athi River Cement Ltd",
            ClientCategory.INDUSTRIAL,
            "0722900111",
            "accounts@athicement.co.ke",
            County.MACHAKOS,
            "Plot 14, Namanga Road, Athi River",
            "-1.4521",
            "36.9785");
    wanjiru =
        client(
            "Wanjiru Kamau",
            ClientCategory.DOMESTIC,
            "0712345678",
            "wanjiru.kamau@gmail.com",
            County.KIAMBU,
            "Kamiti Road, Ruiru",
            "-1.1460",
            "36.9612");
    baraka =
        client(
            "Baraka Hotels Ltd",
            ClientCategory.COMMERCIAL,
            "0733100200",
            "ops@barakahotels.co.ke",
            County.NAKURU,
            "Kenyatta Avenue, Nakuru",
            "-0.2833",
            "36.0667");
    stMarys =
        client(
            "St. Mary's Girls School",
            ClientCategory.COMMERCIAL,
            "0720555010",
            "bursar@stmarysnyeri.ac.ke",
            County.NYERI,
            "Mathari, Nyeri",
            "-0.4201",
            "36.9476");
    otieno =
        client(
            "Otieno Ochieng",
            ClientCategory.DOMESTIC,
            "0701222333",
            "",
            County.KISUMU,
            "Kisian, Kisumu West",
            "",
            "");
    chebet =
        client(
            "Chebet Kiprono",
            ClientCategory.DOMESTIC,
            "0798456123",
            "chebet.kiprono@yahoo.com",
            County.UASIN_GISHU,
            "Kapseret, Eldoret",
            "0.4734",
            "35.2780");
  }

  /** Everything in date order, so job numbers, receipt numbers and timelines read naturally. */
  private void seedJobs() {
    clock.set(at(2026, 9, 22, 10, 0));
    athiRiverJob =
        job(
            athiRiver,
            new QuoteSpec(
                Optional.of(new DrillingSpec(drilling("Symmetric"), Metres.of(150))),
                Optional.of(new PumpSpec(pump("Submersible electric"), Metres.of(10))),
                Optional.of(tank(5000)),
                Optional.of(new Plumbing.Manual(Money.of(50_000), "Plumbing to house and trough"))),
            "Factory canteen and cattle trough. Site visit done by Juma.");

    clock.set(at(2026, 9, 23, 9, 0));
    athiRiverJob = services.jobs().advance(athiRiverJob.id(), JobStatus.APPROVED);
    athiRiverDeposit =
        services
            .payments()
            .record(
                athiRiverJob.id(),
                Money.of(84_400),
                LocalDate.of(2026, 9, 23),
                PaymentMethod.MPESA,
                "SIN7K2QX4P",
                "Deposit");

    clock.set(at(2026, 9, 24, 11, 15));
    wanjiruJob = job(wanjiru, basic(80, "Hand"), "Gate code 2210. Call before arriving.");

    clock.set(at(2026, 9, 25, 8, 0));
    services.jobs().advance(wanjiruJob.id(), JobStatus.APPROVED);
    services
        .payments()
        .record(
            wanjiruJob.id(),
            Money.of(100_000),
            LocalDate.of(2026, 9, 25),
            PaymentMethod.CASH,
            "",
            "Deposit at the office");

    clock.set(at(2026, 9, 28, 7, 45));
    services.jobs().advance(wanjiruJob.id(), JobStatus.IN_PROGRESS);

    clock.set(at(2026, 9, 29, 14, 40));
    barakaJob =
        job(
            baraka,
            new QuoteSpec(
                Optional.of(new DrillingSpec(drilling("Geo-technical"), Metres.of(350))),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()),
            "Second borehole for the new wing.");

    clock.set(at(2026, 10, 1, 9, 5));
    stMarysJob =
        job(
            stMarys,
            new QuoteSpec(
                Optional.of(new DrillingSpec(drilling("Core"), Metres.of(180))),
                Optional.of(new PumpSpec(pump("Solar"), Metres.of(25))),
                Optional.of(tank(10000)),
                Optional.empty()),
            "Dormitory supply.");

    clock.set(at(2026, 10, 2, 10, 30));
    barakaJob = services.jobs().advance(barakaJob.id(), JobStatus.APPROVED);

    clock.set(at(2026, 10, 3, 11, 0));
    services.jobs().advance(stMarysJob.id(), JobStatus.APPROVED);
    services
        .payments()
        .record(
            stMarysJob.id(),
            Money.of(200_000),
            LocalDate.of(2026, 10, 3),
            PaymentMethod.BANK_TRANSFER,
            "FT26276XK91",
            "Deposit");

    clock.set(at(2026, 10, 5, 7, 30));
    athiRiverJob = services.jobs().advance(athiRiverJob.id(), JobStatus.IN_PROGRESS);

    clock.set(at(2026, 10, 9, 17, 10));
    services.jobs().advance(wanjiruJob.id(), JobStatus.COMPLETED);

    clock.set(at(2026, 10, 10, 12, 0));
    services
        .payments()
        .record(
            wanjiruJob.id(),
            Money.of("198120.00"),
            LocalDate.of(2026, 10, 10),
            PaymentMethod.MPESA,
            "SJA3M8RT2V",
            "Balance");
    wanjiruJob = services.jobs().find(wanjiruJob.id()).orElseThrow();

    clock.set(at(2026, 10, 12, 16, 20));
    otienoJob =
        job(
            otieno,
            new QuoteSpec(
                Optional.of(new DrillingSpec(drilling("Symmetric"), Metres.of(120))),
                Optional.of(new PumpSpec(pump("Solar"), Metres.of(20))),
                Optional.of(tank(2000)),
                Optional.empty()),
            "");

    clock.set(at(2026, 10, 14, 15, 0));
    stMarysJob =
        services.jobs().cancel(stMarysJob.id(), "School board postponed the project to 2027.");

    clock.set(at(2026, 10, 15, 10, 45));
    chebetJob = job(chebet, basic(60, "Hand"), "Shared with two neighbouring plots.");
  }

  public List<Job> jobs() {
    return List.of(athiRiverJob, wanjiruJob, barakaJob, stMarysJob, otienoJob, chebetJob);
  }

  private Client client(
      String name,
      ClientCategory category,
      String phone,
      String email,
      County county,
      String address,
      String latitude,
      String longitude) {
    clock.advance(Duration.ofMinutes(7));
    return services
        .clients()
        .register(
            ClientDetails.parse(
                name, category, phone, email, county, address, latitude, longitude));
  }

  private Job job(Client client, QuoteSpec spec, String notes) {
    return services.jobs().create(client.id(), spec, notes);
  }

  private QuoteSpec basic(int depth, String pumpName) {
    return new QuoteSpec(
        Optional.of(new DrillingSpec(drilling("Symmetric"), Metres.of(depth))),
        Optional.of(new PumpSpec(pump(pumpName), Metres.ZERO)),
        Optional.empty(),
        Optional.empty());
  }

  private DrillingType drilling(String name) {
    return services.catalog().activeDrillingTypes().stream()
        .filter(d -> d.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private PumpType pump(String name) {
    return services.catalog().activePumpTypes().stream()
        .filter(p -> p.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private TankType tank(int litres) {
    return services.catalog().activeTankTypes().stream()
        .filter(t -> t.capacityLitres() == litres)
        .findFirst()
        .orElseThrow();
  }

  private static Instant at(int year, int month, int day, int hour, int minute) {
    return LocalDateTime.of(year, month, day, hour, minute).atZone(DisplayFormats.ZONE).toInstant();
  }

  @Override
  public void close() {
    ui.close();
  }
}
