package com.uzima.service;

import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.CatalogKind;
import com.uzima.domain.catalog.CatalogPrices;
import com.uzima.domain.catalog.CategoryFees;
import com.uzima.domain.catalog.RateTable;
import com.uzima.domain.client.ClientCategory;
import java.util.List;
import java.util.Optional;

/** Live catalog prices, fees and rate tiers. Items are retired, never deleted. */
public interface CatalogRepository {

  /** Every item of {@code kind}, retired ones included, in display order. */
  List<CatalogItem> items(CatalogKind kind);

  Optional<CatalogItem> find(CatalogKind kind, long id);

  /** Saves a new item and returns it with its id. Duplicate names are a ValidationException. */
  CatalogItem insert(CatalogItem item);

  /** Saves name/capacity, price and active flag. */
  void update(CatalogItem item);

  /** True when a saved job or plumbing line refers to the item. */
  boolean isReferenced(CatalogItem item);

  RateTable rateTable();

  /** Replaces every tier. Call inside a transaction. */
  void saveRateTable(RateTable table);

  CategoryFees fees(ClientCategory category);

  void saveFees(CategoryFees fees);

  /**
   * Current prices by id, including retired items: a pending job may still hold a retired item, and
   * re-pricing it needs that item's current row.
   */
  CatalogPrices catalogPrices();
}
