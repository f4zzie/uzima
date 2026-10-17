package com.uzima.persistence;

import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.CatalogKind;
import com.uzima.domain.catalog.CatalogPrices;
import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.catalog.RateTier;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.ValidationException;
import com.uzima.persistence.PersistenceException.DuplicateKeyException;
import com.uzima.service.CatalogRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Catalog tables, rate tiers and category fees. */
public final class JdbcCatalogRepository implements CatalogRepository {

  /**
   * Whitelist of the dynamic SQL identifiers. Table and column names come only from here, never
   * from input.
   */
  private enum Table {
    DRILLING("drilling_types", "name", "down_payment", "jobs", "drilling_type_id"),
    PUMP("pump_types", "name", "base_cost", "jobs", "pump_type_id"),
    TANK("tank_types", "capacity_litres", "price", "jobs", "tank_type_id"),
    PIPE("pipe_types", "name", "cost_per_metre", "plumbing_lines", "pipe_type_id");

    final String table;
    final String nameColumn;
    final String priceColumn;
    final String referencingTable;
    final String referencingColumn;

    Table(String table, String name, String price, String refTable, String refColumn) {
      this.table = table;
      this.nameColumn = name;
      this.priceColumn = price;
      this.referencingTable = refTable;
      this.referencingColumn = refColumn;
    }

    static Table of(CatalogKind kind) {
      return valueOf(kind.name());
    }

    String select() {
      return "SELECT id, " + nameColumn + ", " + priceColumn + ", active FROM " + table;
    }
  }

  private final Jdbc jdbc;

  public JdbcCatalogRepository(Jdbc jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<CatalogItem> items(CatalogKind kind) {
    Table t = Table.of(kind);
    return jdbc.query(t.select() + " ORDER BY " + t.nameColumn, rs -> map(kind, t, rs));
  }

  @Override
  public Optional<CatalogItem> find(CatalogKind kind, long id) {
    Table t = Table.of(kind);
    return jdbc.queryOne(t.select() + " WHERE id = ?", rs -> map(kind, t, rs), id);
  }

  @Override
  public CatalogItem insert(CatalogItem item) {
    CatalogKind kind = CatalogKind.of(item);
    Table t = Table.of(kind);
    long id;
    try {
      id =
          jdbc.insert(
              "INSERT INTO "
                  + t.table
                  + " ("
                  + t.nameColumn
                  + ", "
                  + t.priceColumn
                  + ", active) VALUES (?, ?, ?)",
              nameValue(item),
              item.price(),
              item.active());
    } catch (DuplicateKeyException e) {
      throw duplicate(kind);
    }
    return withId(item, id);
  }

  @Override
  public void update(CatalogItem item) {
    CatalogKind kind = CatalogKind.of(item);
    Table t = Table.of(kind);
    int rows;
    try {
      rows =
          jdbc.update(
              "UPDATE "
                  + t.table
                  + " SET "
                  + t.nameColumn
                  + " = ?, "
                  + t.priceColumn
                  + " = ?, active = ? WHERE id = ?",
              nameValue(item),
              item.price(),
              item.active(),
              item.id());
    } catch (DuplicateKeyException e) {
      throw duplicate(kind);
    }
    if (rows != 1) {
      throw new IllegalStateException("No " + kind.label() + " with id " + item.id());
    }
  }

  @Override
  public boolean isReferenced(CatalogItem item) {
    Table t = Table.of(CatalogKind.of(item));
    return jdbc.queryOne(
            "SELECT COUNT(*) FROM " + t.referencingTable + " WHERE " + t.referencingColumn + " = ?",
            rs -> rs.getLong(1) > 0,
            item.id())
        .orElse(false);
  }

  @Override
  public RateTable rateTable() {
    return new RateTable(
        jdbc.query(
            "SELECT from_metres, to_metres, rate_per_metre FROM rate_tiers ORDER BY position",
            rs ->
                new RateTier(
                    Jdbc.metres(rs, "from_metres"),
                    Optional.ofNullable(Jdbc.metres(rs, "to_metres")),
                    Jdbc.money(rs, "rate_per_metre"))));
  }

  @Override
  public void saveRateTable(RateTable table) {
    jdbc.update("DELETE FROM rate_tiers");
    int position = 1;
    for (RateTier tier : table.tiers()) {
      jdbc.update(
          "INSERT INTO rate_tiers (position, from_metres, to_metres, rate_per_metre)"
              + " VALUES (?, ?, ?, ?)",
          position++,
          tier.from(),
          tier.to().orElse(null),
          tier.ratePerMetre());
    }
  }

  @Override
  public CategoryFees fees(ClientCategory category) {
    return jdbc.queryOne(
            "SELECT survey_fee, authority_fee FROM category_fees WHERE category = ?",
            rs ->
                new CategoryFees(
                    category, Jdbc.money(rs, "survey_fee"), Jdbc.money(rs, "authority_fee")),
            category)
        .orElseThrow(() -> new IllegalStateException("No fees configured for " + category));
  }

  @Override
  public void saveFees(CategoryFees fees) {
    int rows =
        jdbc.update(
            "UPDATE category_fees SET survey_fee = ?, authority_fee = ? WHERE category = ?",
            fees.surveyFee(),
            fees.authorityFee(),
            fees.category());
    if (rows == 0) {
      jdbc.update(
          "INSERT INTO category_fees (category, survey_fee, authority_fee) VALUES (?, ?, ?)",
          fees.category(),
          fees.surveyFee(),
          fees.authorityFee());
    }
  }

  @Override
  public CatalogPrices catalogPrices() {
    JdbcCatalogRepository repo = this;
    return new CatalogPrices() {
      @Override
      public Optional<DrillingType> drillingType(long id) {
        return repo.find(CatalogKind.DRILLING, id).map(DrillingType.class::cast);
      }

      @Override
      public Optional<PumpType> pumpType(long id) {
        return repo.find(CatalogKind.PUMP, id).map(PumpType.class::cast);
      }

      @Override
      public Optional<TankType> tankType(long id) {
        return repo.find(CatalogKind.TANK, id).map(TankType.class::cast);
      }

      @Override
      public Optional<PipeType> pipeType(long id) {
        return repo.find(CatalogKind.PIPE, id).map(PipeType.class::cast);
      }
    };
  }

  private static Object nameValue(CatalogItem item) {
    return item instanceof TankType tank ? tank.capacityLitres() : item.displayName();
  }

  private static ValidationException duplicate(CatalogKind kind) {
    return kind == CatalogKind.TANK
        ? new ValidationException("capacityLitres", "A tank with this capacity already exists")
        : new ValidationException(
            "name", "A " + kind.label().toLowerCase() + " with this name already exists");
  }

  private static CatalogItem withId(CatalogItem item, long id) {
    return switch (item) {
      case DrillingType d -> new DrillingType(id, d.name(), d.downPayment(), d.active());
      case PumpType p -> new PumpType(id, p.name(), p.baseCost(), p.active());
      case TankType tk -> new TankType(id, tk.capacityLitres(), tk.price(), tk.active());
      case PipeType p -> new PipeType(id, p.name(), p.costPerMetre(), p.active());
    };
  }

  private static CatalogItem map(CatalogKind kind, Table t, ResultSet rs) throws SQLException {
    long id = rs.getLong("id");
    boolean active = rs.getBoolean("active");
    var price = Jdbc.money(rs, t.priceColumn);
    return switch (kind) {
      case DRILLING -> new DrillingType(id, rs.getString(t.nameColumn), price, active);
      case PUMP -> new PumpType(id, rs.getString(t.nameColumn), price, active);
      case TANK -> new TankType(id, rs.getInt(t.nameColumn), price, active);
      case PIPE -> new PipeType(id, rs.getString(t.nameColumn), price, active);
    };
  }
}
