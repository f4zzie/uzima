package com.uzima.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.domain.common.Metres;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.PaymentStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompositionRootTest {

  @TempDir Path dataDir;

  @Test
  void startCreatesEmbeddedDatabaseFileInAppDataDirectory() {
    try (CompositionRoot root =
        CompositionRoot.start(AppConfig.load(dataDir.resolve("nested"), Map.of()))) {
      assertThat(root.databaseDescription()).startsWith("H2 ");
      assertThat(root.config().dataDir()).isEqualTo(dataDir.resolve("nested"));
    }
    assertThat(Files.exists(dataDir.resolve("nested/data/uzima.mv.db"))).isTrue();
  }

  /** Smoke test of the real wiring: first run through to a paid quote for acceptance example 1. */
  @Test
  void wiredServicesRunTheFirstRunToPaymentFlow() {
    try (CompositionRoot root = CompositionRoot.start(AppConfig.load(dataDir, Map.of()))) {
      Services s = root.services();

      assertThat(s.auth().needsAdministrator()).isTrue();
      s.auth().createAdministrator("admin", "Office Admin", "correct horse battery".toCharArray());
      assertThat(s.session().currentUser()).isPresent();

      Client client =
          s.clients()
              .register(
                  ClientDetails.parse(
                      "Thika Plastics Ltd",
                      ClientCategory.INDUSTRIAL,
                      "0712 345 678",
                      "",
                      County.KIAMBU,
                      "Thika",
                      "",
                      ""));
      assertThat(client.code().value()).isEqualTo("UZM-IND-0001");

      var symmetric =
          s.catalog().activeDrillingTypes().stream()
              .filter(d -> d.name().equals("Symmetric"))
              .findFirst()
              .orElseThrow();
      var submersible =
          s.catalog().activePumpTypes().stream()
              .filter(p -> p.name().equals("Submersible electric"))
              .findFirst()
              .orElseThrow();
      var tank5000 =
          s.catalog().activeTankTypes().stream()
              .filter(t -> t.capacityLitres() == 5_000)
              .findFirst()
              .orElseThrow();
      QuoteSpec spec =
          new QuoteSpec(
              Optional.of(new DrillingSpec(symmetric, Metres.of(150))),
              Optional.of(new PumpSpec(submersible, Metres.of(10))),
              Optional.of(tank5000),
              Optional.of(new Plumbing.Manual(Money.of(50_000), "Plumbing works")));

      Job job = s.jobs().create(client.id(), spec, "");
      assertThat(job.number().value()).startsWith("JOB-");
      assertThat(job.quote().subtotal()).isEqualTo(Money.of("590000.00"));
      assertThat(job.quote().tax()).isEqualTo(Money.of("94400.00"));
      assertThat(job.quote().grandTotal()).isEqualTo(Money.of("684400.00"));

      s.payments()
          .record(
              job.id(),
              Money.of(84_400),
              LocalDate.now(com.uzima.domain.common.DisplayFormats.ZONE),
              PaymentMethod.MPESA,
              "QJK3H5L7PZ",
              "");
      var account = s.payments().account(job.id());
      assertThat(account.balance()).isEqualTo(Money.of(600_000));
      assertThat(account.status()).isEqualTo(PaymentStatus.PARTIALLY_PAID);
    }
  }
}
