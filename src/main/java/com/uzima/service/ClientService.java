package com.uzima.service;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.User;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Registering and maintaining clients. Clients are never deleted: they are deactivated, and the
 * foreign keys from jobs stop a client with jobs from being removed even by hand.
 */
public final class ClientService {

  static final String ENTITY = "Client";

  private final ClientRepository clients;
  private final SequenceGenerator sequences;
  private final Transactions tx;
  private final Session session;
  private final AuditLog audit;
  private final Clock clock;

  public ClientService(
      ClientRepository clients,
      SequenceGenerator sequences,
      Transactions tx,
      Session session,
      AuditLog audit,
      Clock clock) {
    this.clients = clients;
    this.sequences = sequences;
    this.tx = tx;
    this.session = session;
    this.audit = audit;
    this.clock = clock;
  }

  /** Registers a client and issues the next {@code UZM-XXX-NNNN} code for its category. */
  public Client register(ClientDetails details) {
    User user = session.require(Permission.EDIT_CLIENTS);
    return tx.inTransaction(
        () -> {
          ClientCode code = sequences.nextClientCode(details.category());
          Client saved =
              clients.insert(new Client(0, code, details, true, clock.instant()), user.id());
          audit.record(
              AuditAction.CLIENT_CREATED,
              ENTITY,
              code,
              details.name() + " (" + details.category().label() + ")");
          return saved;
        });
  }

  /** Saves edited details. The category is changed with {@link #changeCategory}. */
  public Client update(long id, ClientDetails details) {
    session.require(Permission.EDIT_CLIENTS);
    return tx.inTransaction(
        () -> {
          Client before = load(id);
          Client after = before.withDetails(details);
          Changes changes = describe(before.details(), after.details());
          if (changes.isEmpty()) {
            return before;
          }
          clients.update(after);
          audit.record(AuditAction.CLIENT_UPDATED, ENTITY, before.code(), changes.toString());
          return after;
        });
  }

  /**
   * Moves a client to another category. The client ID keeps its original prefix; saved jobs keep
   * the fees they were quoted with, and only new quotes (or an explicit re-price) use the new
   * category's fees.
   */
  public Client changeCategory(long id, ClientCategory category) {
    session.require(Permission.CHANGE_CLIENT_CATEGORY);
    return tx.inTransaction(
        () -> {
          Client before = load(id);
          Client after = before.withCategory(category);
          clients.update(after);
          audit.record(
              AuditAction.CLIENT_CATEGORY_CHANGED,
              ENTITY,
              before.code(),
              new Changes()
                  .field("category", before.category().label(), category.label())
                  .toString());
          return after;
        });
  }

  public Client deactivate(long id) {
    return setActive(id, false);
  }

  public Client reactivate(long id) {
    return setActive(id, true);
  }

  public Optional<Client> find(long id) {
    session.requireUser();
    return clients.findById(id);
  }

  public List<ClientSummary> search(ClientSearch search) {
    session.requireUser();
    return clients.search(search);
  }

  private Client setActive(long id, boolean active) {
    session.require(Permission.DEACTIVATE_CLIENTS);
    return tx.inTransaction(
        () -> {
          Client before = load(id);
          if (before.active() == active) {
            return before;
          }
          Client after = active ? before.reactivated() : before.deactivated();
          clients.update(after);
          audit.record(
              active ? AuditAction.CLIENT_REACTIVATED : AuditAction.CLIENT_DEACTIVATED,
              ENTITY,
              before.code(),
              before.name());
          return after;
        });
  }

  private Client load(long id) {
    return clients.findById(id).orElseThrow(() -> new DomainException("Client not found."));
  }

  private static Changes describe(ClientDetails b, ClientDetails a) {
    return new Changes()
        .field("name", b.name(), a.name())
        .field("phone", b.phone(), a.phone())
        .field("email", b.email().orElse(null), a.email().orElse(null))
        .field("county", b.county(), a.county())
        .field("address", b.address(), a.address())
        .field("location", location(b), location(a));
  }

  /** Compares coordinates by value, so 36.80 and 36.800000 (as stored) are the same place. */
  private static String location(ClientDetails d) {
    return d.location()
        .map(
            l ->
                l.latitude().stripTrailingZeros().toPlainString()
                    + ", "
                    + l.longitude().stripTrailingZeros().toPlainString())
        .orElse(null);
  }
}
