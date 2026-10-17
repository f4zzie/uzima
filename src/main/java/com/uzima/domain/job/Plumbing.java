package com.uzima.domain.job;

import com.uzima.domain.common.Validator;
import com.uzima.domain.money.Money;
import java.util.List;

/** Optional plumbing on a job: either itemised pipe runs or a manual lump sum. */
public sealed interface Plumbing permits Plumbing.Itemised, Plumbing.Manual {

  /**
   * Pipe runs priced per metre, plus the number of outlets for the whole plumbing job. Outlets are
   * recorded but do not currently affect the price.
   */
  record Itemised(List<PipeRun> runs, int outlets) implements Plumbing {

    public Itemised {
      runs = runs == null ? List.of() : List.copyOf(runs);
      new Validator()
          .check(!runs.isEmpty(), "plumbingRuns", "Add at least one pipe run")
          .check(outlets >= 0, "outlets", "Outlets cannot be negative")
          .throwIfInvalid();
    }
  }

  /** A single lump-sum amount with a description. */
  record Manual(Money amount, String description) implements Plumbing {

    public Manual {
      new Validator()
          .check(amount != null && amount.isPositive(), "plumbingAmount", "Enter an amount above 0")
          .require(description, "plumbingDescription", "Describe the plumbing work")
          .throwIfInvalid();
      description = description.strip();
    }
  }
}
