package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.persistence.TestDatabases.Kind;
import com.uzima.service.ClientSearch;
import com.uzima.service.ClientSummary;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JdbcClientRepositoryTest {

  private static final Instant CREATED = Instant.parse("2026-10-01T07:00:00Z");

  private static ClientDetails details(
      String name, ClientCategory category, String phone, County county) {
    return ClientDetails.parse(name, category, phone, "", county, "", "", "");
  }

  private static Client client(int seq, ClientDetails details) {
    return new Client(0, new ClientCode(details.category(), seq), details, true, CREATED);
  }

  private record Fixture(Database db, JdbcClientRepository repo, long userId)
      implements AutoCloseable {
    static Fixture open(Kind kind) {
      Database db = TestDatabases.open(kind);
      return new Fixture(
          db,
          new JdbcClientRepository(new Jdbc(new JdbcTransactions(db.dataSource()))),
          TestDatabases.insertUser(db, "clerk", "CLERK"));
    }

    @Override
    public void close() {
      db.close();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void roundTripsAllFieldsIncludingOptionalOnes(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      ClientDetails full =
          ClientDetails.parse(
              "Kamau Water Works",
              ClientCategory.COMMERCIAL,
              "0712 345 678",
              "Info@Kamau.co.ke",
              County.NAKURU,
              "Box 12, Naivasha",
              "-0.7167",
              "36.4333");
      Client saved = f.repo.insert(client(1, full), f.userId);

      Client loaded = f.repo.findById(saved.id()).orElseThrow();
      assertThat(saved.id()).isPositive();
      assertThat(loaded.code().value()).isEqualTo("UZM-COM-0001");
      assertThat(loaded.name()).isEqualTo("Kamau Water Works");
      assertThat(loaded.details().phone().value()).isEqualTo("+254712345678");
      assertThat(loaded.details().email().orElseThrow().value()).isEqualTo("info@kamau.co.ke");
      assertThat(loaded.details().county()).isEqualTo(County.NAKURU);
      assertThat(loaded.details().address()).isEqualTo("Box 12, Naivasha");
      assertThat(loaded.details().location().orElseThrow().latitude())
          .isEqualByComparingTo(new BigDecimal("-0.7167"));
      assertThat(loaded.details().location().orElseThrow().longitude())
          .isEqualByComparingTo(new BigDecimal("36.4333"));
      assertThat(loaded.createdAt()).isEqualTo(CREATED);
      assertThat(loaded.active()).isTrue();

      Client minimal =
          f.repo.insert(
              client(1, details("Otieno", ClientCategory.DOMESTIC, "0722000111", County.KISUMU)),
              f.userId);
      Client minimalLoaded = f.repo.findByCode(minimal.code()).orElseThrow();
      assertThat(minimalLoaded.details().email()).isEmpty();
      assertThat(minimalLoaded.details().location()).isEmpty();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void updateSavesDetailsCategoryAndActiveButNeverTheCode(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      Client saved =
          f.repo.insert(
              client(7, details("Achieng", ClientCategory.DOMESTIC, "0711111111", County.SIAYA)),
              f.userId);
      Client changed = saved.withCategory(ClientCategory.INDUSTRIAL).deactivated();
      f.repo.update(changed);

      Client loaded = f.repo.findById(saved.id()).orElseThrow();
      assertThat(loaded.category()).isEqualTo(ClientCategory.INDUSTRIAL);
      assertThat(loaded.code().value()).isEqualTo("UZM-DOM-0007");
      assertThat(loaded.active()).isFalse();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void searchFiltersByTextCategoryCountyAndActive(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      f.repo.insert(
          client(1, details("Zawadi Farm", ClientCategory.DOMESTIC, "0712000001", County.KIAMBU)),
          f.userId);
      Client mill =
          f.repo.insert(
              client(
                  1,
                  details("Mumias Mill", ClientCategory.INDUSTRIAL, "0712000002", County.KAKAMEGA)),
              f.userId);
      f.repo.insert(
          client(2, details("Amani Hotel", ClientCategory.DOMESTIC, "0799000003", County.KIAMBU)),
          f.userId);
      f.repo.update(mill.deactivated());

      assertThat(names(f, ClientSearch.all()))
          .containsExactly("Amani Hotel", "Mumias Mill", "Zawadi Farm");
      assertThat(names(f, ClientSearch.all().withText("mill"))).containsExactly("Mumias Mill");
      assertThat(names(f, ClientSearch.all().withText("uzm-ind"))).containsExactly("Mumias Mill");
      assertThat(names(f, ClientSearch.all().withText("0799 000"))).containsExactly("Amani Hotel");
      assertThat(names(f, ClientSearch.all().withText("100%"))).isEmpty();
      assertThat(names(f, ClientSearch.all().withCategory(ClientCategory.DOMESTIC)))
          .containsExactly("Amani Hotel", "Zawadi Farm");
      assertThat(names(f, ClientSearch.all().withCounty(County.KAKAMEGA)))
          .containsExactly("Mumias Mill");
      assertThat(names(f, ClientSearch.all().withActive(true)))
          .containsExactly("Amani Hotel", "Zawadi Farm");
      assertThat(names(f, ClientSearch.all().withActive(false))).containsExactly("Mumias Mill");
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void reportsJobsAndCountsRegistrations(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      Client saved =
          f.repo.insert(
              client(1, details("Wanjiru", ClientCategory.DOMESTIC, "0712000009", County.NYERI)),
              f.userId);
      assertThat(f.repo.hasJobs(saved.id())).isFalse();

      PersistenceTestRows.insertJob(f.db, saved.id(), f.userId, null);
      assertThat(f.repo.hasJobs(saved.id())).isTrue();

      assertThat(f.repo.countCreatedBetween(CREATED, CREATED.plusSeconds(1))).isEqualTo(1);
      assertThat(f.repo.countCreatedBetween(CREATED.plusSeconds(1), CREATED.plusSeconds(60)))
          .isZero();
    }
  }

  private static java.util.List<String> names(Fixture f, ClientSearch search) {
    return f.repo.search(search).stream().map(ClientSummary::name).toList();
  }
}
