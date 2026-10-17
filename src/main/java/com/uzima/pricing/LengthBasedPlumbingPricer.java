package com.uzima.pricing;

import com.uzima.domain.job.LineCategory;
import com.uzima.domain.job.PipeRun;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.QuantityUnit;
import com.uzima.domain.job.QuoteLine;
import com.uzima.domain.money.Money;
import java.util.List;

/**
 * Default plumbing pricing: one line per pipe run, cost per metre of the pipe type × length.
 * Diameter and outlets are recorded on the job but do not change the price.
 */
public final class LengthBasedPlumbingPricer implements PlumbingLinePricer {

  @Override
  public List<QuoteLine> price(Plumbing.Itemised plumbing) {
    return plumbing.runs().stream().map(LengthBasedPlumbingPricer::line).toList();
  }

  private static QuoteLine line(PipeRun run) {
    Money perMetre = run.pipe().costPerMetre();
    return new QuoteLine(
        LineCategory.PLUMBING,
        run.pipe().name() + " pipe, " + run.diameterMm() + " mm",
        run.length().value(),
        QuantityUnit.METRE,
        perMetre,
        perMetre.times(run.length().value()));
  }
}
