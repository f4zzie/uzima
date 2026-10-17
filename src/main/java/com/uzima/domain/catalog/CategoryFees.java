package com.uzima.domain.catalog;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.common.Validator;
import com.uzima.domain.money.Money;
import java.util.Objects;

/** Survey and authority fees charged on every job for clients of one category. */
public record CategoryFees(ClientCategory category, Money surveyFee, Money authorityFee) {

  public CategoryFees {
    Objects.requireNonNull(category, "category");
    new Validator()
        .check(
            surveyFee != null && !surveyFee.isNegative(),
            "surveyFee",
            "Survey fee cannot be negative")
        .check(
            authorityFee != null && !authorityFee.isNegative(),
            "authorityFee",
            "Authority fee cannot be negative")
        .throwIfInvalid();
  }
}
