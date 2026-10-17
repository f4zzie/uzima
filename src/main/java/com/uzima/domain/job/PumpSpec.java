package com.uzima.domain.job;

import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.common.Metres;
import java.util.Objects;

/** Pump selection: the pump model and the lift height to the tank, in metres. */
public record PumpSpec(PumpType type, Metres liftHeight) {

  public PumpSpec {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(liftHeight, "liftHeight");
  }
}
