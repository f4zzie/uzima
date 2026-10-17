package com.uzima.service;

import com.uzima.domain.job.JobStatus;
import java.time.Instant;
import java.util.Optional;

/**
 * One row of a job's status timeline. {@code from} is empty for the first entry, written when the
 * job was created.
 */
public record StatusHistoryEntry(
    Optional<JobStatus> from, JobStatus to, String reason, Instant at, long changedBy) {}
