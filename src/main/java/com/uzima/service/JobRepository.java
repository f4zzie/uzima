package com.uzima.service;

import com.uzima.domain.job.Job;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.job.StatusChange;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Jobs with their full price snapshot: spec, price basis (fees, tiers, tax rate), quote lines and
 * itemised plumbing. Write methods must run inside {@link Transactions#inTransaction}.
 */
public interface JobRepository {

  /**
   * Saves a new job (id 0) with its lines, plumbing runs and tier snapshot, plus the first status
   * history entry (no previous status → the job's status, at its creation time). Returns the job
   * with its generated id.
   */
  Job insert(Job job);

  /**
   * Replaces the spec, price basis, quote lines, totals and notes of a saved job. Used for edits
   * while awaiting survey and for an applied re-pricing.
   */
  void replaceQuote(Job job, Instant at);

  /** Saves a status transition: updates the job's status and appends to its history. */
  void recordStatusChange(long jobId, StatusChange change);

  void updateNotes(long jobId, String notes, Instant at);

  Optional<Job> findById(long id);

  Optional<Job> findByNumber(JobNumber number);

  /** Status timeline, oldest first. */
  List<StatusHistoryEntry> history(long jobId);

  /** Jobs list with derived payment state, newest first. */
  List<JobSummary> search(JobSearch search);

  Optional<JobSummary> summary(long jobId);
}
