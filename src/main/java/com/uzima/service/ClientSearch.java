package com.uzima.service;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.County;
import java.util.Optional;

/**
 * Filters for the client list. {@code text} matches the client ID, name or phone number; empty
 * values match everything.
 */
public record ClientSearch(
    Optional<String> text,
    Optional<ClientCategory> category,
    Optional<County> county,
    Optional<Boolean> active,
    int limit) {

  public static final int DEFAULT_LIMIT = 500;

  public static ClientSearch all() {
    return new ClientSearch(
        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), DEFAULT_LIMIT);
  }

  public ClientSearch withText(String value) {
    return new ClientSearch(Optional.ofNullable(value), category, county, active, limit);
  }

  public ClientSearch withCategory(ClientCategory value) {
    return new ClientSearch(text, Optional.ofNullable(value), county, active, limit);
  }

  public ClientSearch withCounty(County value) {
    return new ClientSearch(text, category, Optional.ofNullable(value), active, limit);
  }

  public ClientSearch withActive(Boolean value) {
    return new ClientSearch(text, category, county, Optional.ofNullable(value), limit);
  }
}
