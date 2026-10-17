package com.uzima.service;

import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.StatusChange;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Forwards every call; tests override one method to inject a failure. */
class DelegatingJobRepository implements JobRepository {

  private final JobRepository delegate;

  DelegatingJobRepository(JobRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Job insert(Job job) {
    return delegate.insert(job);
  }

  @Override
  public void replaceQuote(Job job, Instant at) {
    delegate.replaceQuote(job, at);
  }

  @Override
  public void recordStatusChange(long jobId, StatusChange change) {
    delegate.recordStatusChange(jobId, change);
  }

  @Override
  public void updateNotes(long jobId, String notes, Instant at) {
    delegate.updateNotes(jobId, notes, at);
  }

  @Override
  public Optional<Job> findById(long id) {
    return delegate.findById(id);
  }

  @Override
  public Optional<Job> findByNumber(JobNumber number) {
    return delegate.findByNumber(number);
  }

  @Override
  public List<StatusHistoryEntry> history(long jobId) {
    return delegate.history(jobId);
  }

  @Override
  public List<JobSummary> search(JobSearch search) {
    return delegate.search(search);
  }

  @Override
  public Optional<JobSummary> summary(long jobId) {
    return delegate.summary(jobId);
  }

  @Override
  public void lock(long jobId) {
    delegate.lock(jobId);
  }
}
