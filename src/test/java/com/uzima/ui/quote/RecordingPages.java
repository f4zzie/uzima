package com.uzima.ui.quote;

import com.uzima.ui.Pages;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/** A {@link Pages} that only records what was opened. */
final class RecordingPages implements Pages {

  final List<String> calls = new ArrayList<>();

  @Override
  public void clients() {
    calls.add("clients");
  }

  @Override
  public void client(long clientId) {
    calls.add("client " + clientId);
  }

  @Override
  public void jobs() {
    calls.add("jobs");
  }

  @Override
  public void job(long jobId) {
    calls.add("job " + jobId);
  }

  @Override
  public void newJob(OptionalLong clientId) {
    calls.add("newJob " + clientId);
  }

  @Override
  public void editJob(long jobId) {
    calls.add("editJob " + jobId);
  }
}
