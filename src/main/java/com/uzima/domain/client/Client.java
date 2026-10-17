package com.uzima.domain.client;

import com.uzima.domain.common.ValidationException;
import java.time.Instant;
import java.util.Objects;

/**
 * A registered client. The category is fixed at registration because it is part of the client ID; a
 * client whose category is wrong is deactivated and registered again.
 */
public record Client(
    long id, ClientCode code, ClientDetails details, boolean active, Instant createdAt) {

  public Client {
    Objects.requireNonNull(code, "code");
    Objects.requireNonNull(details, "details");
    Objects.requireNonNull(createdAt, "createdAt");
  }

  public ClientCategory category() {
    return details.category();
  }

  public String name() {
    return details.name();
  }

  /**
   * Applies edited details. The category is changed only through {@link #withCategory}, which needs
   * its own permission and audit entry.
   */
  public Client withDetails(ClientDetails updated) {
    if (updated.category() != category()) {
      throw new ValidationException("category", "Use \"Change category\" to change the category");
    }
    return new Client(id, code, updated, active, createdAt);
  }

  /**
   * Moves the client to another category. The client ID does not change, and saved jobs keep the
   * fees they were quoted with; only new quotes use the new category's fees.
   */
  public Client withCategory(ClientCategory newCategory) {
    if (newCategory == category()) {
      throw new ValidationException("category", "The client is already " + newCategory.label());
    }
    ClientDetails d = details;
    return new Client(
        id,
        code,
        new ClientDetails(
            d.name(), newCategory, d.phone(), d.email(), d.county(), d.address(), d.location()),
        active,
        createdAt);
  }

  public Client deactivated() {
    return new Client(id, code, details, false, createdAt);
  }

  public Client reactivated() {
    return new Client(id, code, details, true, createdAt);
  }
}
