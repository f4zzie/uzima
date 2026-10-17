package com.uzima.domain.job;

import com.uzima.domain.common.ValidationException;
import java.time.Year;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Job number {@code JOB-{yyyy}-{NNNNN}}, sequential per year. Numbers past 99999 widen. */
public record JobNumber(Year year, int sequence) {

  private static final Pattern FORMAT = Pattern.compile("JOB-(\\d{4})-(\\d{5,9})");

  public JobNumber {
    Objects.requireNonNull(year, "year");
    if (sequence < 1) {
      throw new IllegalArgumentException("Job sequence must be positive: " + sequence);
    }
  }

  public static JobNumber parse(String text) {
    Matcher m = FORMAT.matcher(text == null ? "" : text.strip());
    if (!m.matches() || Integer.parseInt(m.group(2)) < 1) {
      throw new ValidationException("jobNumber", "Job numbers look like JOB-2026-00042");
    }
    return new JobNumber(Year.parse(m.group(1)), Integer.parseInt(m.group(2)));
  }

  public String value() {
    return "JOB-%s-%05d".formatted(year, sequence);
  }

  @Override
  public String toString() {
    return value();
  }
}
