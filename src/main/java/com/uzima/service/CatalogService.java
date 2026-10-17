package com.uzima.service;

import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.CatalogKind;
import com.uzima.domain.catalog.CatalogPrices;
import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.job.PriceBasis;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.Permission;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Catalog prices, fees and rate tiers. Any signed-in user can read them; changing them needs {@link
 * Permission#EDIT_CATALOG} and is audited with old and new values. Changes affect new quotes only:
 * saved jobs keep the prices they were quoted with.
 */
public final class CatalogService {

  private final CatalogRepository catalog;
  private final Transactions tx;
  private final Session session;
  private final AuditLog audit;

  public CatalogService(
      CatalogRepository catalog, Transactions tx, Session session, AuditLog audit) {
    this.catalog = catalog;
    this.tx = tx;
    this.session = session;
    this.audit = audit;
  }

  // --- Reads ----------------------------------------------------------------------------------

  /** Every item of {@code kind}, retired ones included. */
  public List<CatalogItem> items(CatalogKind kind) {
    session.requireUser();
    return catalog.items(kind);
  }

  /** Items that can be chosen on a new quote. */
  public List<CatalogItem> activeItems(CatalogKind kind) {
    return items(kind).stream().filter(CatalogItem::active).toList();
  }

  public List<DrillingType> activeDrillingTypes() {
    return activeItems(CatalogKind.DRILLING).stream().map(DrillingType.class::cast).toList();
  }

  public List<PumpType> activePumpTypes() {
    return activeItems(CatalogKind.PUMP).stream().map(PumpType.class::cast).toList();
  }

  public List<TankType> activeTankTypes() {
    return activeItems(CatalogKind.TANK).stream().map(TankType.class::cast).toList();
  }

  public List<PipeType> activePipeTypes() {
    return activeItems(CatalogKind.PIPE).stream().map(PipeType.class::cast).toList();
  }

  public RateTable rateTable() {
    session.requireUser();
    return catalog.rateTable();
  }

  public CategoryFees fees(ClientCategory category) {
    session.requireUser();
    return catalog.fees(category);
  }

  /** The fees, tiers and tax rate a new quote for a client of {@code category} is priced with. */
  public PriceBasis currentBasis(ClientCategory category, TaxRate taxRate) {
    session.requireUser();
    return new PriceBasis(catalog.fees(category), catalog.rateTable(), taxRate);
  }

  /** Current item prices by id, retired items included (used when re-pricing a pending job). */
  public CatalogPrices currentPrices() {
    session.requireUser();
    return catalog.catalogPrices();
  }

  public boolean isInUse(CatalogItem item) {
    session.requireUser();
    return catalog.isReferenced(item);
  }

  // --- Writes ---------------------------------------------------------------------------------

  public CatalogItem addItem(CatalogItem item) {
    session.require(Permission.EDIT_CATALOG);
    return tx.inTransaction(
        () -> {
          CatalogItem saved = catalog.insert(item);
          audit.record(
              AuditAction.CATALOG_ITEM_CREATED,
              entity(saved),
              saved.id(),
              saved.displayName()
                  + ", "
                  + CatalogKind.of(saved).priceLabel().toLowerCase()
                  + " "
                  + saved.price().format());
          return saved;
        });
  }

  /** Saves a new name (or capacity) and price. The active flag is unchanged. */
  public CatalogItem changeItem(CatalogItem updated) {
    session.require(Permission.EDIT_CATALOG);
    return tx.inTransaction(
        () -> {
          CatalogItem before = load(updated);
          CatalogItem after = withActive(updated, before.active());
          Changes changes =
              new Changes()
                  .field("name", before.displayName(), after.displayName())
                  .field("price", before.price().format(), after.price().format());
          if (changes.isEmpty()) {
            return before;
          }
          catalog.update(after);
          audit.record(
              AuditAction.CATALOG_ITEM_CHANGED,
              entity(after),
              after.id(),
              after.displayName() + ": " + changes);
          return after;
        });
  }

  /** Hides an item from new quotes. Saved jobs that use it are unaffected. */
  public CatalogItem retire(CatalogKind kind, long id) {
    return setActive(kind, id, false);
  }

  public CatalogItem reactivate(CatalogKind kind, long id) {
    return setActive(kind, id, true);
  }

  /** Replaces the rate tiers. The table's constructor has already validated its shape. */
  public RateTable saveRateTable(RateTable table) {
    session.require(Permission.EDIT_CATALOG);
    return tx.inTransaction(
        () -> {
          RateTable before = catalog.rateTable();
          if (before.equals(table)) {
            return before;
          }
          catalog.saveRateTable(table);
          audit.record(
              AuditAction.RATE_TIERS_CHANGED,
              "RateTable",
              "",
              new Changes().field("tiers", describe(before), describe(table)).toString());
          return table;
        });
  }

  public CategoryFees saveFees(CategoryFees fees) {
    session.require(Permission.EDIT_CATALOG);
    return tx.inTransaction(
        () -> {
          CategoryFees before = catalog.fees(fees.category());
          Changes changes =
              new Changes()
                  .field("survey fee", before.surveyFee().format(), fees.surveyFee().format())
                  .field(
                      "authority fee",
                      before.authorityFee().format(),
                      fees.authorityFee().format());
          if (changes.isEmpty()) {
            return before;
          }
          catalog.saveFees(fees);
          audit.record(
              AuditAction.CATEGORY_FEES_CHANGED,
              "CategoryFees",
              fees.category(),
              fees.category().label() + ": " + changes);
          return fees;
        });
  }

  private CatalogItem setActive(CatalogKind kind, long id, boolean active) {
    session.require(Permission.EDIT_CATALOG);
    return tx.inTransaction(
        () -> {
          CatalogItem before =
              catalog
                  .find(kind, id)
                  .orElseThrow(() -> new DomainException(kind.label() + " not found."));
          if (before.active() == active) {
            return before;
          }
          CatalogItem after = withActive(before, active);
          catalog.update(after);
          audit.record(
              active ? AuditAction.CATALOG_ITEM_REACTIVATED : AuditAction.CATALOG_ITEM_RETIRED,
              entity(after),
              id,
              after.displayName());
          return after;
        });
  }

  private CatalogItem load(CatalogItem item) {
    CatalogKind kind = CatalogKind.of(item);
    return catalog
        .find(kind, item.id())
        .orElseThrow(() -> new DomainException(kind.label() + " not found."));
  }

  private static CatalogItem withActive(CatalogItem item, boolean active) {
    return switch (item) {
      case DrillingType d -> new DrillingType(d.id(), d.name(), d.downPayment(), active);
      case PumpType p -> new PumpType(p.id(), p.name(), p.baseCost(), active);
      case TankType t -> new TankType(t.id(), t.capacityLitres(), t.price(), active);
      case PipeType p -> new PipeType(p.id(), p.name(), p.costPerMetre(), active);
    };
  }

  private static String entity(CatalogItem item) {
    return item.getClass().getSimpleName();
  }

  /** "0–100 m @ KSh 1,000.00; …; 300 m and above @ KSh 2,500.00". */
  static String describe(RateTable table) {
    return table.tiers().stream()
        .map(t -> t.label() + " @ " + t.ratePerMetre().format())
        .collect(Collectors.joining("; "));
  }
}
