package com.uzima.ui.clients;

import com.uzima.domain.job.JobStatus;
import com.uzima.domain.money.Money;
import com.uzima.service.JobSummary;
import java.util.List;

/**
 * A client's figures, summed from the service's job summaries (only {@link Money#sum}; each value
 * is already worked out by the service). Cancelled jobs owe nothing and aren't counted as quoted,
 * but money received on them still counts as paid and shows as a refund due.
 *
 * @param jobs every job, cancelled ones included
 * @param quoted grand totals of jobs that aren't cancelled
 * @param paid net amount received across all jobs
 * @param balance still owed (cancelled jobs owe nothing)
 * @param refundDue received on cancelled jobs and not yet refunded
 */
record ClientTotals(int jobs, Money quoted, Money paid, Money balance, Money refundDue) {

  static ClientTotals of(List<JobSummary> summaries) {
    return new ClientTotals(
        summaries.size(),
        Money.sum(
            summaries.stream()
                .filter(j -> j.status() != JobStatus.CANCELLED)
                .map(JobSummary::grandTotal)),
        Money.sum(summaries.stream().map(JobSummary::amountPaid)),
        Money.sum(summaries.stream().map(JobSummary::balance)),
        Money.sum(summaries.stream().map(JobSummary::refundDue)));
  }
}
