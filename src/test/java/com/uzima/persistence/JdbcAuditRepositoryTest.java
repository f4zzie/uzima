package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.service.AuditAction;
import com.uzima.service.AuditEntry;
import com.uzima.service.AuditQuery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcAuditRepositoryTest {

  private static final Instant DAY1 = Instant.parse("2026-10-01T08:00:00Z");
  private static final Instant DAY2 = Instant.parse("2026-10-02T08:00:00Z");
  private static final Instant DAY3 = Instant.parse("2026-10-03T08:00:00Z");

  private Database db;
  private JdbcAuditRepository repo;
  private long alice;
  private long bob;

  @BeforeEach
  void setUp() {
    db = TestDatabases.h2();
    repo = new JdbcAuditRepository(new Jdbc(new JdbcTransactions(db.dataSource())));
    alice = TestDatabases.insertUser(db, "alice", "ADMIN");
    bob = TestDatabases.insertUser(db, "bob", "CLERK");
    repo.append(entry(DAY1, alice, "alice", AuditAction.LOGIN_SUCCEEDED, ""));
    repo.append(entry(DAY2, bob, "bob", AuditAction.CLIENT_CREATED, "name: 100% Pure_Water"));
    repo.append(entry(DAY3, null, "mallory", AuditAction.LOGIN_FAILED, "Unknown username"));
  }

  @AfterEach
  void tearDown() {
    db.close();
  }

  private static AuditEntry entry(
      Instant at, Long userId, String username, AuditAction action, String details) {
    return new AuditEntry(0, at, userId, username, action, "user", "", details);
  }

  private List<String> usernames(AuditQuery q) {
    return repo.search(q).stream().map(AuditEntry::username).toList();
  }

  private static AuditQuery query(
      Optional<Instant> from,
      Optional<Instant> to,
      Optional<AuditAction> action,
      Optional<Long> user,
      Optional<String> text) {
    return new AuditQuery(from, to, action, user, text, 100);
  }

  @Test
  void returnsNewestFirstAndRoundTripsFields() {
    List<AuditEntry> all = repo.search(AuditQuery.latest(100));
    assertThat(all).extracting(AuditEntry::username).containsExactly("mallory", "bob", "alice");
    assertThat(all.getFirst().userId()).isNull();
    assertThat(all.getFirst().occurredAt()).isEqualTo(DAY3);
    assertThat(all.get(1).userId()).isEqualTo(bob);
    assertThat(all.get(1).action()).isEqualTo(AuditAction.CLIENT_CREATED);
  }

  @Test
  void filtersByHalfOpenDateRange() {
    assertThat(
            usernames(
                query(
                    Optional.of(DAY2),
                    Optional.of(DAY3),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty())))
        .containsExactly("bob");
  }

  @Test
  void filtersByActionAndUser() {
    assertThat(
            usernames(
                query(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(AuditAction.LOGIN_FAILED),
                    Optional.empty(),
                    Optional.empty())))
        .containsExactly("mallory");
    assertThat(
            usernames(
                query(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(alice),
                    Optional.empty())))
        .containsExactly("alice");
  }

  @Test
  void textSearchIsCaseInsensitiveAndTreatsWildcardsLiterally() {
    assertThat(usernames(query(none(), none(), none(), none(), Optional.of("PURE_water"))))
        .containsExactly("bob");
    assertThat(usernames(query(none(), none(), none(), none(), Optional.of("100%"))))
        .containsExactly("bob");
    // "_" and "%" must not act as wildcards: "e_w" would otherwise match "Pure_Water" and others.
    assertThat(usernames(query(none(), none(), none(), none(), Optional.of("%")))).hasSize(1);
    assertThat(usernames(query(none(), none(), none(), none(), Optional.of("MALL"))))
        .containsExactly("mallory");
    assertThat(usernames(query(none(), none(), none(), none(), Optional.of("  ")))).hasSize(3);
  }

  @Test
  void limitsResults() {
    assertThat(repo.search(AuditQuery.latest(2))).hasSize(2);
    assertThat(repo.search(AuditQuery.latest(0))).hasSize(1);
  }

  @Test
  void escapesLikeMetacharacters() {
    assertThat(JdbcAuditRepository.escapeLike("a!b%c_d")).isEqualTo("a!!b!%c!_d");
  }

  private static <T> Optional<T> none() {
    return Optional.empty();
  }
}
