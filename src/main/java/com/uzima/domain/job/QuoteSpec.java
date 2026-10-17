package com.uzima.domain.job;

import com.uzima.domain.catalog.TankType;
import com.uzima.domain.common.ValidationException;
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
}
