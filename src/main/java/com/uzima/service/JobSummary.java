package com.uzima.service;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.money.Money;
import com.uzima.domain.payment.PaymentStatus;
import java.time.Instant;

/**
 * A row of the jobs list: the job's snapshot totals plus payment state derived from the payments
 * table (the {@code job_accounts} view), never stored on the job. {@code clientCategory} is the
 * client's current category, which may differ from the category the job was priced with.
 */
public record JobSummary(
    long id,
    JobNumber number,
    long clientId,
    ClientCode clientCode,
    String clientName,
    ClientCategory clientCategory,
    JobStatus status,
    Money grandTotal,
    Money amountPaid,
    Money balance,
    PaymentStatus paymentStatus,
    Money refundDue,
    Instant createdAt) {}
