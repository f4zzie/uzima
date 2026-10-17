package com.uzima.domain.job;

import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.common.Metres;
import java.util.Objects;

/** Drilling selection: the method and the borehole depth. */
public record DrillingSpec(DrillingType type, Metres depth) {

  public DrillingSpec {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(depth, "depth");
  }
}
