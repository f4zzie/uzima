package com.uzima.pricing;

import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.QuoteLine;
import java.util.List;

/**
 * Prices itemised plumbing. This is the extension point for plumbing rules: today only pipe type
 * and length affect the price, but an implementation may also charge by diameter or per outlet (for
 * example by returning an extra outlet line). Lines must use {@code LineCategory.PLUMBING}.
 */
public interface PlumbingLinePricer {

  List<QuoteLine> price(Plumbing.Itemised plumbing);
}
