package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.CatalogKind;
import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.Money;
import com.uzima.persistence.TestDatabases.Kind;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JdbcCatalogRepositoryTest {

  private record Fixture(Database db, JdbcCatalogRepository repo) implements AutoCloseable {
    static Fixture open(Kind kind) {
      Database db = TestDatabases.open(kind);
      return new Fixture(
          db, new JdbcCatalogRepository(new Jdbc(new JdbcTransactions(db.dataSource()))));
    }

    @Override
    public void close() {
      db.close();
    }
  }

  private static List<String> summary(List<CatalogItem> items) {
    return items.stream().map(i -> i.displayName() + "=" + i.price().formatPlain()).toList();
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void referenceDataMatchesTheBusinessRules(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      assertThat(summary(f.repo.items(CatalogKind.DRILLING)))
          .containsExactly("Core=225,000.00", "Geo-technical=335,000.00", "Symmetric=130,000.00");
      assertThat(summary(f.repo.items(CatalogKind.PUMP)))
          .containsExactly("Hand=30,000.00", "Solar=65,000.00", "Submersible electric=90,000.00");
      assertThat(summary(f.repo.items(CatalogKind.TANK)))
          .containsExactly(
              "1,000 L=15,000.00", "2,000 L=28,000.00", "5,000 L=65,000.00", "10,000 L=125,000.00");
      assertThat(summary(f.repo.items(CatalogKind.PIPE)))
          .containsExactly("Galvanized=1,200.00", "HDPE=800.00", "PVC=500.00");
      assertThat(f.repo.rateTable())
          .isEqualTo(
              new RateTable(
                  List.of(
                      RateTier.bounded(0, 100, 1_000),
                      RateTier.bounded(100, 200, 1_500),
                      RateTier.bounded(200, 300, 2_000),
                      RateTier.openEnded(300, 2_500))));
      assertThat(f.repo.fees(ClientCategory.INDUSTRIAL))
          .isEqualTo(
              new CategoryFees(ClientCategory.INDUSTRIAL, Money.of(20_000), Money.of(50_000)));
      assertThat(f.repo.fees(ClientCategory.COMMERCIAL))
          .isEqualTo(
              new CategoryFees(ClientCategory.COMMERCIAL, Money.of(15_000), Money.of(30_000)));
      assertThat(f.repo.fees(ClientCategory.DOMESTIC))
          .isEqualTo(new CategoryFees(ClientCategory.DOMESTIC, Money.of(7_000), Money.of(10_000)));
      assertThat(f.repo.items(CatalogKind.DRILLING)).allMatch(CatalogItem::active);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void insertsUpdatesAndFindsEveryKind(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      List<CatalogItem> fresh =
          List.of(
              new DrillingType(0, "Percussion", Money.of(150_000), true),
              new PumpType(0, "Wind", Money.of(70_000), true),
              new TankType(0, 3_000, Money.of(40_000), true),
              new PipeType(0, "Copper", Money.of(2_000), true));
      for (CatalogItem item : fresh) {
        CatalogItem saved = f.repo.insert(item);
        CatalogKind k = CatalogKind.of(item);
        assertThat(saved.id()).isPositive();
        assertThat(f.repo.find(k, saved.id())).contains(saved);

        CatalogItem repriced =
            switch (saved) {
              case DrillingType d -> d.withPrice(Money.of(1)).retired();
              case PumpType p -> p.withPrice(Money.of(1)).retired();
              case TankType t -> t.withPrice(Money.of(1)).retired();
              case PipeType p -> p.withPrice(Money.of(1)).retired();
            };
        f.repo.update(repriced);
        assertThat(f.repo.find(k, saved.id())).contains(repriced);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void duplicateNamesAndCapacitiesAreValidationErrors(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      assertThatThrownBy(() -> f.repo.insert(new PumpType(0, "Solar", Money.of(1), true)))
          .isInstanceOfSatisfying(
              ValidationException.class, e -> assertThat(e.messageFor("name")).isPresent());
      assertThatThrownBy(() -> f.repo.insert(new TankType(0, 5_000, Money.of(1), true)))
          .isInstanceOfSatisfying(
              ValidationException.class,
              e -> assertThat(e.messageFor("capacityLitres")).isPresent());
      PumpType hand = (PumpType) f.repo.items(CatalogKind.PUMP).getFirst();
      assertThatThrownBy(
              () -> f.repo.update(new PumpType(hand.id(), "Solar", hand.baseCost(), true)))
          .isInstanceOf(ValidationException.class);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void savesRateTablesAndFees(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      RateTable two =
          new RateTable(List.of(RateTier.bounded(0, 50, 900), RateTier.openEnded(50, 1_800)));
      f.repo.saveRateTable(two);
      assertThat(f.repo.rateTable()).isEqualTo(two);

      CategoryFees fees =
          new CategoryFees(ClientCategory.DOMESTIC, Money.of("7500.50"), Money.of(11_000));
      f.repo.saveFees(fees);
      assertThat(f.repo.fees(ClientCategory.DOMESTIC)).isEqualTo(fees);
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void knowsWhenAnItemIsReferencedByAJob(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      long user = TestDatabases.insertUser(f.db, "clerk", "CLERK");
      Jdbc jdbc = new Jdbc(new JdbcTransactions(f.db.dataSource()));
      Client client =
          new JdbcClientRepository(jdbc)
              .insert(
                  new Client(
                      0,
                      new ClientCode(ClientCategory.DOMESTIC, 1),
                      ClientDetails.parse(
                          "A", ClientCategory.DOMESTIC, "0712000000", "", County.MERU, "", "", ""),
                      true,
                      Instant.EPOCH),
                  user);
      DrillingType symmetric =
          (DrillingType)
              f.repo.items(CatalogKind.DRILLING).stream()
                  .filter(i -> i.displayName().equals("Symmetric"))
                  .findFirst()
                  .orElseThrow();
      DrillingType core = (DrillingType) f.repo.items(CatalogKind.DRILLING).getFirst();

      PersistenceTestRows.insertJob(f.db, client.id(), user, symmetric.id());

      assertThat(f.repo.isReferenced(symmetric)).isTrue();
      assertThat(f.repo.isReferenced(core)).isFalse();
      PipeType pvc = (PipeType) f.repo.items(CatalogKind.PIPE).getLast();
      assertThat(f.repo.isReferenced(pvc)).isFalse();
    }
  }

  @ParameterizedTest
  @EnumSource(Kind.class)
  void catalogPricesIncludeRetiredItems(Kind kind) {
    try (Fixture f = Fixture.open(kind)) {
      TankType tank = (TankType) f.repo.items(CatalogKind.TANK).getFirst();
      f.repo.update(tank.withPrice(Money.of(16_000)).retired());

      var prices = f.repo.catalogPrices();
      assertThat(prices.tankType(tank.id()).orElseThrow().price()).isEqualTo(Money.of(16_000));
      assertThat(prices.tankType(tank.id()).orElseThrow().active()).isFalse();
      assertThat(prices.drillingType(999)).isEmpty();
      assertThat(prices.pumpType(f.repo.items(CatalogKind.PUMP).getFirst().id())).isPresent();
      assertThat(prices.pipeType(f.repo.items(CatalogKind.PIPE).getFirst().id())).isPresent();
    }
  }
}
