package com.uzima.domain.catalog;

import java.util.Optional;

/**
 * Looks up catalog items by id, at whatever prices the source holds: the live catalog, or the items
 * captured in a saved job's snapshot.
 */
public interface CatalogPrices {

  Optional<DrillingType> drillingType(long id);

  Optional<PumpType> pumpType(long id);

  Optional<TankType> tankType(long id);

  Optional<PipeType> pipeType(long id);
}
