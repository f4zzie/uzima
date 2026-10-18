package com.uzima.ui;

import java.util.OptionalLong;

/**
 * Opens the app's screens by what they show. Pages link to one another through this (a job's
 * client, a client's jobs) without depending on each other's classes; {@code AppPages} builds the
 * real screens.
 */
public interface Pages {

  void clients();

  void client(long clientId);

  void jobs();

  void job(long jobId);

  /** The quote editor for a new job, for {@code clientId} when given. */
  void newJob(OptionalLong clientId);

  /** The quote editor for an existing job (only while it is pending survey). */
  void editJob(long jobId);
}
