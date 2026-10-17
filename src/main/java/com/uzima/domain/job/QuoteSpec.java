package com.uzima.domain.job;

import com.uzima.domain.catalog.CatalogPrices;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.common.ValidationException;
import java.util.List;
import java.util.Optional;

/**
 * What the client asked for. Any combination of services is allowed, but at least one is required.
 * Survey and authority fees are not part of the spec: they always apply.
 */
public record QuoteSpec(
    Optional<DrillingSpec> drilling,
    Optional<PumpSpec> pump,
    Optional<TankType> tank,
    Optional<Plumbing> plumbing) {

  public QuoteSpec {
    drilling = drilling == null ? Optional.empty() : drilling;
    pump = pump == null ? Optional.empty() : pump;
    tank = tank == null ? Optional.empty() : tank;
    plumbing = plumbing == null ? Optional.empty() : plumbing;
    if (drilling.isEmpty() && pump.isEmpty() && tank.isEmpty() && plumbing.isEmpty()) {
      throw new ValidationException(
          "services", "Select at least one service: drilling, pump, tank or plumbing");
    }
  }

  /** The catalog items in this spec, at the prices they carry, looked up by id. */
  public CatalogPrices catalogPrices() {
    return new CatalogPrices() {
      @Override
      public Optional<DrillingType> drillingType(long id) {
        return drilling.map(DrillingSpec::type).filter(t -> t.id() == id);
      }

      @Override
      public Optional<PumpType> pumpType(long id) {
        return pump.map(PumpSpec::type).filter(t -> t.id() == id);
      }

      @Override
      public Optional<TankType> tankType(long id) {
        return tank.filter(t -> t.id() == id);
      }

      @Override
      public Optional<PipeType> pipeType(long id) {
        return pipeRuns().stream().map(PipeRun::pipe).filter(p -> p.id() == id).findFirst();
      }
    };
  }

  /**
   * Replaces each catalog item with the version {@code prices} holds for the same id, keeping the
   * item as it is when {@code prices} has none. Quantities (depth, lift height, pipe lengths) are
   * untouched.
   */
  public QuoteSpec withPricesFrom(CatalogPrices prices) {
    return new QuoteSpec(
        drilling.map(
            d -> new DrillingSpec(prices.drillingType(d.type().id()).orElse(d.type()), d.depth())),
        pump.map(
            p -> new PumpSpec(prices.pumpType(p.type().id()).orElse(p.type()), p.liftHeight())),
        tank.map(t -> prices.tankType(t.id()).orElse(t)),
        plumbing.map(
            p ->
                switch (p) {
                  case Plumbing.Itemised itemised ->
                      new Plumbing.Itemised(
                          itemised.runs().stream()
                              .map(
                                  r ->
                                      new PipeRun(
                                          prices.pipeType(r.pipe().id()).orElse(r.pipe()),
                                          r.diameterMm(),
                                          r.length()))
                              .toList(),
                          itemised.outlets());
                  case Plumbing.Manual manual -> manual;
                }));
  }

  private List<PipeRun> pipeRuns() {
    return plumbing
        .filter(Plumbing.Itemised.class::isInstance)
        .map(p -> ((Plumbing.Itemised) p).runs())
        .orElse(List.of());
  }
}
