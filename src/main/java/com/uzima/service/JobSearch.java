package com.uzima.service;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.job.JobStatus;
import com.uzima.domain.payment.PaymentStatus;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Filters for the jobs list. Empty values match everything. Dates are calendar days in
 * Africa/Nairobi and both ends are inclusive. {@code text} matches the job number, client name or
 * client code.
 */
public record JobSearch(
    Optional<JobStatus> status,
    Optional<PaymentStatus> paymentStatus,
    Optional<LocalDate> createdFrom,
    Optional<LocalDate> createdTo,
    Optional<Long> clientId,
    Optional<ClientCategory> clientCategory,
    Optional<String> text,
    int limit) {

  public static final int DEFAULT_LIMIT = 500;

  public JobSearch {
    status = status == null ? Optional.empty() : status;
    paymentStatus = paymentStatus == null ? Optional.empty() : paymentStatus;
    createdFrom = createdFrom == null ? Optional.empty() : createdFrom;
    createdTo = createdTo == null ? Optional.empty() : createdTo;
    clientId = clientId == null ? Optional.empty() : clientId;
    clientCategory = clientCategory == null ? Optional.empty() : clientCategory;
    text = text == null ? Optional.empty() : text.filter(t -> !t.isBlank()).map(String::strip);
    if (limit < 1) {
      throw new IllegalArgumentException("limit must be positive");
    }
  }

  /** Every job, newest first, up to {@link #DEFAULT_LIMIT}. */
  public static JobSearch all() {
    return new JobSearch(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        DEFAULT_LIMIT);
  }

  public JobSearch withStatus(JobStatus value) {
    return new JobSearch(
        Optional.of(value),
        paymentStatus,
        createdFrom,
        createdTo,
        clientId,
        clientCategory,
        text,
        limit);
  }

  public JobSearch withPaymentStatus(PaymentStatus value) {
    return new JobSearch(
        status, Optional.of(value), createdFrom, createdTo, clientId, clientCategory, text, limit);
  }

  public JobSearch withCreatedBetween(LocalDate from, LocalDate to) {
    return new JobSearch(
        status,
        paymentStatus,
        Optional.ofNullable(from),
        Optional.ofNullable(to),
        clientId,
        clientCategory,
        text,
        limit);
  }

  public JobSearch withClient(long id) {
    return new JobSearch(
        status,
        paymentStatus,
        createdFrom,
        createdTo,
        Optional.of(id),
        clientCategory,
        text,
        limit);
  }

  public JobSearch withClientCategory(ClientCategory value) {
    return new JobSearch(
        status, paymentStatus, createdFrom, createdTo, clientId, Optional.of(value), text, limit);
  }

  public JobSearch withText(String value) {
    return new JobSearch(
        status,
        paymentStatus,
        createdFrom,
        createdTo,
        clientId,
        clientCategory,
        Optional.ofNullable(value),
        limit);
  }

  public JobSearch withLimit(int value) {
    return new JobSearch(
        status, paymentStatus, createdFrom, createdTo, clientId, clientCategory, text, value);
  }
}
