package com.uzima.domain.job;

import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.common.Metres;
import com.uzima.domain.common.Validator;

/**
 * One itemised plumbing line: a length of pipe of one type and diameter. The diameter is recorded
 * for the job record; pricing currently uses only the pipe type and length (see {@code
 * PlumbingLinePricer}).
 */
public record PipeRun(PipeType pipe, int diameterMm, Metres length) {

  public PipeRun {
    new Validator()
        .check(pipe != null, "pipe", "Choose a pipe type")
        .check(diameterMm > 0, "diameterMm", "Diameter must be a positive number of millimetres")
        .check(length != null && !length.isZero(), "length", "Length must be greater than 0 m")
        .throwIfInvalid();
  }
}
