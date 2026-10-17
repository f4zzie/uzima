package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.CatalogKind;
import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.money.Money;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Role;
import com.uzima.persistence.JdbcCatalogRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {

  private ServiceTestContext ctx;
  private JdbcCatalogRepository repository;
  private CatalogService service;

  @BeforeEach
  void setUp() {
    ctx = ServiceTestContext.h2();
    repository = new JdbcCatalogRepository(ctx.jdbc);
    service = new CatalogService(repository, ctx.tx, ctx.session, ctx.audit);
  }

  @AfterEach
  void tearDown() {
    ctx.close();
  }

  private DrillingType symmetric() {
    return service.activeDrillingTypes().stream()
        .filter(d -> d.name().equals("Symmetric"))
        .findFirst()
        .orElseThrow();
  }

  @Test
  void priceChangeIsSavedAndAuditedWithOldAndNewValues() {
    ctx.signInAs(Role.MANAGER);
    DrillingType before = symmetric();

    service.changeItem(before.withPrice(Money.of(140_000)));

    assertThat(symmetric().downPayment()).isEqualTo(Money.of(140_000));
    AuditEntry entry = ctx.auditEntries().getFirst();
    assertThat(entry.action()).isEqualTo(AuditAction.CATALOG_ITEM_CHANGED);
    assertThat(entry.entityType()).isEqualTo("DrillingType");
    assertThat(entry.entityId()).isEqualTo(Long.toString(before.id()));
    assertThat(entry.details()).isEqualTo("Symmetric: price: KSh 130,000.00 → KSh 140,000.00");
  }

  @Test
  void unchangedItemWritesNoAuditEntry() {
    ctx.signInAs(Role.MANAGER);
    service.changeItem(symmetric());
    assertThat(ctx.auditEntries()).isEmpty();
  }

  @Test
  void changeKeepsTheStoredActiveFlag() {
    ctx.signInAs(Role.MANAGER);
    DrillingType d = symmetric();
    service.retire(CatalogKind.DRILLING, d.id());

    service.changeItem(new DrillingType(d.id(), "Symmetric", Money.of(1), true));

    assertThat(repository.find(CatalogKind.DRILLING, d.id()).orElseThrow().active()).isFalse();
  }

  @Test
  void retiredItemsDisappearFromNewQuotesButStayListed() {
    ctx.signInAs(Role.MANAGER);
    TankType tank = service.activeTankTypes().getFirst();

    service.retire(CatalogKind.TANK, tank.id());

    assertThat(service.activeTankTypes()).extracting(TankType::id).doesNotContain(tank.id());
    assertThat(service.items(CatalogKind.TANK)).extracting(CatalogItem::id).contains(tank.id());
    assertThat(ctx.auditEntries().getFirst().action()).isEqualTo(AuditAction.CATALOG_ITEM_RETIRED);
    assertThat(ctx.auditEntries().getFirst().details()).isEqualTo("1,000 L");

    service.reactivate(CatalogKind.TANK, tank.id());
    assertThat(service.activeTankTypes()).extracting(TankType::id).contains(tank.id());
    assertThat(ctx.auditEntries().getFirst().action())
        .isEqualTo(AuditAction.CATALOG_ITEM_REACTIVATED);
  }

  @Test
  void addItemIsAuditedAndDuplicatesAreFieldErrors() {
    ctx.signInAs(Role.ADMIN);
    CatalogItem saved = service.addItem(new PumpType(0, "Wind", Money.of(70_000), true));

    assertThat(service.activePumpTypes()).extracting(PumpType::id).contains(saved.id());
    assertThat(ctx.auditEntries().getFirst().details()).isEqualTo("Wind, base cost KSh 70,000.00");
    assertThatThrownBy(() -> service.addItem(new PumpType(0, "Wind", Money.of(1), true)))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("name")).isPresent());
  }

  @Test
  void clerksCannotEditTheCatalogButCanReadIt() {
    ctx.signInAs(Role.CLERK);
    DrillingType d = symmetric();

    assertThatThrownBy(() -> service.changeItem(d.withPrice(Money.of(1))))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> service.retire(CatalogKind.DRILLING, d.id()))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> service.saveRateTable(service.rateTable()))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(
            () ->
                service.saveFees(new CategoryFees(ClientCategory.DOMESTIC, Money.ZERO, Money.ZERO)))
        .isInstanceOf(AccessDeniedException.class);
    assertThat(symmetric().downPayment()).isEqualTo(Money.of(130_000));
    assertThat(service.activePipeTypes()).hasSize(3);
  }

  @Test
  void rateTableChangeIsAuditedWithOldAndNewTiers() {
    ctx.signInAs(Role.MANAGER);
    RateTable two =
        new RateTable(List.of(RateTier.bounded(0, 150, 1_100), RateTier.openEnded(150, 2_000)));

    service.saveRateTable(two);

    assertThat(service.rateTable()).isEqualTo(two);
    AuditEntry entry = ctx.auditEntries().getFirst();
    assertThat(entry.action()).isEqualTo(AuditAction.RATE_TIERS_CHANGED);
    assertThat(entry.details())
        .isEqualTo(
            "tiers: 0–100 m @ KSh 1,000.00; 100–200 m @ KSh 1,500.00; 200–300 m @ KSh 2,000.00;"
                + " 300 m and above @ KSh 2,500.00 → 0–150 m @ KSh 1,100.00;"
                + " 150 m and above @ KSh 2,000.00");
  }

  @Test
  void invalidTierShapesNeverReachTheDatabase() {
    ctx.signInAs(Role.MANAGER);
    RateTable before = service.rateTable();

    assertThatThrownBy(
            () ->
                service.saveRateTable(
                    new RateTable(
                        List.of(RateTier.bounded(0, 100, 1_000), RateTier.openEnded(120, 2_000)))))
        .isInstanceOf(ValidationException.class);

    assertThat(service.rateTable()).isEqualTo(before);
    assertThat(ctx.auditEntries()).isEmpty();
  }

  @Test
  void feesChangeIsAudited() {
    ctx.signInAs(Role.MANAGER);
    service.saveFees(new CategoryFees(ClientCategory.DOMESTIC, Money.of(8_000), Money.of(10_000)));

    assertThat(service.fees(ClientCategory.DOMESTIC).surveyFee()).isEqualTo(Money.of(8_000));
    AuditEntry entry = ctx.auditEntries().getFirst();
    assertThat(entry.action()).isEqualTo(AuditAction.CATEGORY_FEES_CHANGED);
    assertThat(entry.entityId()).isEqualTo("DOMESTIC");
    assertThat(entry.details()).isEqualTo("Domestic: survey fee: KSh 7,000.00 → KSh 8,000.00");
  }

  @Test
  void currentBasisCombinesCategoryFeesTiersAndTaxRate() {
    ctx.signInAs(Role.CLERK);
    PriceBasis basis = service.currentBasis(ClientCategory.INDUSTRIAL, TaxRate.of("0.16"));

    assertThat(basis.fees().surveyFee()).isEqualTo(Money.of(20_000));
    assertThat(basis.rates().tiers()).hasSize(4);
    assertThat(basis.taxRate()).isEqualTo(TaxRate.KENYA_STANDARD);
    assertThat(service.currentPrices().drillingType(symmetric().id())).contains(symmetric());
    assertThat(service.isInUse(symmetric())).isFalse();
  }

  @Test
  void failedAuditWriteRollsBackTheCatalogChange() {
    ctx.signInAs(Role.MANAGER);
    AuditRepository failing =
        new AuditRepository() {
          @Override
          public long append(AuditEntry entry) {
            throw new IllegalStateException("audit store unavailable");
          }

          @Override
          public List<AuditEntry> search(AuditQuery query) {
            return List.of();
          }
        };
    CatalogService broken =
        new CatalogService(
            repository, ctx.tx, ctx.session, new AuditLog(failing, ctx.session, ctx.clock));
    RateTable tiersBefore = service.rateTable();

    assertThatThrownBy(() -> broken.changeItem(symmetric().withPrice(Money.of(1))))
        .hasMessage("audit store unavailable");
    assertThatThrownBy(() -> broken.saveRateTable(new RateTable(List.of(RateTier.openEnded(0, 5)))))
        .hasMessage("audit store unavailable");

    assertThat(symmetric().downPayment()).isEqualTo(Money.of(130_000));
    assertThat(service.rateTable()).isEqualTo(tiersBefore);
  }

  @Test
  void unknownItemIsReported() {
    ctx.signInAs(Role.MANAGER);
    assertThatThrownBy(() -> service.retire(CatalogKind.PIPE, 999))
        .isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> service.changeItem(new PumpType(999, "Ghost", Money.of(1), true)))
        .isInstanceOf(DomainException.class);
  }
}
