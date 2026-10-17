package com.uzima.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final String HASH =
      "$2a$12$abcdefghijklmnopqrstuuVwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0";
  private static final Instant T0 = Instant.parse("2026-10-18T08:00:00Z");

  private static User clerk() {
    return User.create(new Username("clerk1"), "Grace Achieng", Role.CLERK, HASH, false);
  }

  private static User failTimes(User user, int times, Instant at) {
    User u = user;
    for (int i = 0; i < times; i++) {
      u = u.recordFailedLogin(at);
    }
    return u;
  }

  @Test
  void createProducesActiveUnsavedUnlockedUser() {
    User u = clerk();
    assertThat(u.id()).isZero();
    assertThat(u.active()).isTrue();
    assertThat(u.failedLoginAttempts()).isZero();
    assertThat(u.lockedUntil()).isEmpty();
    assertThat(u.isLockedAt(T0)).isFalse();
  }

  @Test
  void fourFailuresDoNotLock() {
    User u = failTimes(clerk(), 4, T0);
    assertThat(u.failedLoginAttempts()).isEqualTo(4);
    assertThat(u.isLockedAt(T0)).isFalse();
  }

  @Test
  void fifthFailureLocksForExactlyFifteenMinutes() {
    User u = failTimes(clerk(), 5, T0);

    assertThat(u.lockedUntil()).contains(T0.plus(Duration.ofMinutes(15)));
    assertThat(u.failedLoginAttempts()).isZero();
    assertThat(u.isLockedAt(T0)).isTrue();
    assertThat(u.isLockedAt(T0.plus(Duration.ofMinutes(14).plusSeconds(59)))).isTrue();
    assertThat(u.isLockedAt(T0.plus(Duration.ofMinutes(15)))).isFalse();
  }

  @Test
  void failuresWhileLockedDoNotExtendTheLock() {
    User locked = failTimes(clerk(), 5, T0);
    User again = locked.recordFailedLogin(T0.plus(Duration.ofMinutes(10)));
    assertThat(again).isEqualTo(locked);
  }

  @Test
  void counterStartsFreshAfterLockExpires() {
    Instant later = T0.plus(User.LOCK_DURATION);
    User u = failTimes(clerk(), 5, T0).recordFailedLogin(later);

    assertThat(u.failedLoginAttempts()).isEqualTo(1);
    assertThat(u.lockedUntil()).isEmpty();
    assertThat(u.isLockedAt(later)).isFalse();
  }

  @Test
  void successResetsCounterAndLock() {
    assertThat(failTimes(clerk(), 3, T0).recordSuccessfulLogin().failedLoginAttempts()).isZero();
    User unlocked = failTimes(clerk(), 5, T0).recordSuccessfulLogin();
    assertThat(unlocked.lockedUntil()).isEmpty();
    assertThat(unlocked.isLockedAt(T0)).isFalse();
  }

  @Test
  void newPasswordHashClearsLockAndSetsMustChange() {
    User reset = failTimes(clerk(), 5, T0).withPasswordHash("$2a$12$new", true);
    assertThat(reset.passwordHash()).isEqualTo("$2a$12$new");
    assertThat(reset.mustChangePassword()).isTrue();
    assertThat(reset.isLockedAt(T0)).isFalse();
  }

  @Test
  void roleAndActiveFlagChanges() {
    User u = clerk();
    assertThat(u.withRole(Role.MANAGER).role()).isEqualTo(Role.MANAGER);
    assertThat(u.deactivated().active()).isFalse();
    assertThat(u.deactivated().reactivated().active()).isTrue();
  }

  @Test
  void toStringHidesPasswordHash() {
    assertThat(clerk().toString()).doesNotContain(HASH).doesNotContain("$2a$").contains("clerk1");
  }

  @Test
  void displayNameRequiredAndTrimmed() {
    assertThat(
            User.create(new Username("abc"), "  Peter Otieno ", Role.ADMIN, HASH, false)
                .displayName())
        .isEqualTo("Peter Otieno");
    assertThatThrownBy(() -> User.create(new Username("abc"), " ", Role.ADMIN, HASH, false))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("displayName")).isPresent());
    assertThatThrownBy(
            () -> User.create(new Username("abc"), "x".repeat(81), Role.ADMIN, HASH, false))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void negativeFailureCountRejected() {
    assertThatThrownBy(
            () ->
                new User(
                    1,
                    new Username("abc"),
                    "A",
                    Role.CLERK,
                    HASH,
                    true,
                    false,
                    -1,
                    Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
