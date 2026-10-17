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
    if (code.category() != details.category()) {
      throw new IllegalArgumentException("Client code category does not match client category");
    }
  }

  public ClientCategory category() {
    return details.category();
  }

  public String name() {
    return details.name();
  }

  /** Applies edited details. The category cannot change. */
  public Client withDetails(ClientDetails updated) {
    if (updated.category() != category()) {
      throw new ValidationException(
          "category", "A client's category cannot change after registration");
    }
    return new Client(id, code, updated, active, createdAt);
  }

  public Client deactivated() {
    return new Client(id, code, details, false, createdAt);
  }

  public Client reactivated() {
    return new Client(id, code, details, true, createdAt);
  }
}
