package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.user.Role;
import com.uzima.domain.user.User;
import com.uzima.domain.user.Username;
import com.uzima.service.Theme;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcUserRepositoryTest {

  private static final Instant NOW = Instant.parse("2026-10-01T07:00:00.123Z");

  private Database db;
  private JdbcUserRepository repo;

  @BeforeEach
  void setUp() {
    db = TestDatabases.h2();
    repo = new JdbcUserRepository(new Jdbc(new JdbcTransactions(db.dataSource())));
  }

  @AfterEach
  void tearDown() {
    db.close();
  }

  private User insert(String name, Role role) {
    return repo.insert(User.create(new Username(name), name, role, "$2a$hash", false), NOW);
  }

  @Test
  void insertsAndFindsByIdAndUsername() {
    User saved = insert("wanjiku", Role.MANAGER);

    assertThat(saved.id()).isPositive();
    assertThat(repo.findById(saved.id())).contains(saved);
    assertThat(repo.findByUsername(new Username("WANJIKU"))).contains(saved);
    assertThat(repo.findById(999)).isEmpty();
    assertThat(repo.count()).isEqualTo(1);
  }

  @Test
  void updatesEveryMutableFieldIncludingLockout() {
    User saved = insert("otieno", Role.CLERK);
    Instant lockedUntil = Instant.parse("2026-10-01T07:15:00.123Z");
    User changed =
        new User(
            saved.id(),
            saved.username(),
            "Otieno O.",
            Role.ADMIN,
            "$2a$other",
            false,
            true,
            3,
            Optional.of(lockedUntil));

    repo.update(changed);

    assertThat(repo.findById(saved.id())).contains(changed);
    repo.update(changed.recordSuccessfulLogin());
    assertThat(repo.findById(saved.id()).orElseThrow().lockedUntil()).isEmpty();
  }

  @Test
  void updatingAMissingUserFails() {
    User ghost =
        new User(
            42, new Username("ghost"), "Ghost", Role.CLERK, "h", true, false, 0, Optional.empty());
    assertThatThrownBy(() -> repo.update(ghost)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void listsActiveUsersFirstAndCountsActiveAdmins() {
    User zed = insert("zed", Role.ADMIN);
    insert("amy", Role.ADMIN);
    insert("bea", Role.CLERK);
    repo.update(zed.deactivated());

    assertThat(repo.findAll())
        .extracting(u -> u.username().value())
        .containsExactly("amy", "bea", "zed");
    assertThat(repo.countActiveAdmins()).isEqualTo(1);
  }

  @Test
  void themeDefaultsToLightAndIsStoredPerUser() {
    User a = insert("amy", Role.ADMIN);
    User b = insert("bea", Role.CLERK);

    repo.setTheme(a.id(), Theme.DARK);

    assertThat(repo.theme(a.id())).isEqualTo(Theme.DARK);
    assertThat(repo.theme(b.id())).isEqualTo(Theme.LIGHT);
    assertThatThrownBy(() -> repo.theme(999)).isInstanceOf(IllegalStateException.class);
  }
}
