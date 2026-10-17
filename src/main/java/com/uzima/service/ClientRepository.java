package com.uzima.service;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Persistence for clients. Clients are deactivated, never deleted. */
public interface ClientRepository {

  /** Saves a new client and returns it with its database id. */
  Client insert(Client client, long createdBy);

  /** Saves details, category and active flag. The client code never changes. */
  void update(Client client);

  Optional<Client> findById(long id);

  Optional<Client> findByCode(ClientCode code);

  /** Ordered by name. */
  List<ClientSummary> search(ClientSearch search);

  boolean hasJobs(long clientId);

  /** Clients registered in {@code [from, to)}. */
  int countCreatedBetween(Instant from, Instant to);
}
