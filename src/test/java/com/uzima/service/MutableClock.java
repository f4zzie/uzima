package com.uzima.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** A clock tests can move forward, e.g. to expire a login lockout. */
public final class MutableClock extends Clock {

  private Instant now;
  private final ZoneId zone;

  public MutableClock(Instant start, ZoneId zone) {
    this.now = start;
    this.zone = zone;
  }

  public void advance(Duration d) {
    now = now.plus(d);
  }

  public void set(Instant instant) {
    now = instant;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId z) {
    return new MutableClock(now, z);
  }

  @Override
  public Instant instant() {
    return now;
  }
}
