package com.uzima.domain.job;

import static com.uzima.domain.job.JobStatus.APPROVED;
import static com.uzima.domain.job.JobStatus.CANCELLED;
import static com.uzima.domain.job.JobStatus.COMPLETED;
import static com.uzima.domain.job.JobStatus.IN_PROGRESS;
import static com.uzima.domain.job.JobStatus.PENDING_SURVEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class StatusChangeTest {

  private static final Instant AT = Instant.parse("2026-10-18T07:00:00Z");
  private static final long USER = 7;

  private static final Set<String> ALLOWED =
      Set.of(
          "PENDING_SURVEY->APPROVED",
          "APPROVED->IN_PROGRESS",
          "IN_PROGRESS->COMPLETED",
          "PENDING_SURVEY->CANCELLED",
          "APPROVED->CANCELLED",
          "IN_PROGRESS->CANCELLED",
          "CANCELLED->PENDING_SURVEY");

  static Stream<Arguments> everyPair() {
    return Arrays.stream(JobStatus.values())
        .flatMap(
            from ->
                Arrays.stream(JobStatus.values())
                    .map(
                        to ->
                            Arguments.of(
                                from, to, ALLOWED.contains(from.name() + "->" + to.name()))));
  }

  /** Requests a change through whichever API applies to the target state. */
  private static StatusChange attempt(JobStatus from, JobStatus to) {
    if (to == CANCELLED) {
      return StatusChange.cancel(from, "Client withdrew", AT, USER);
    }
    if (from == CANCELLED && to == PENDING_SURVEY) {
      return StatusChange.reopen(from, "Client came back", AT, USER);
    }
    return StatusChange.advance(from, to, AT, USER);
  }

  @ParameterizedTest(name = "{0} -> {1} allowed={2}")
  @MethodSource("everyPair")
  void enforcesLifecycleForEveryPairOfStates(JobStatus from, JobStatus to, boolean allowed) {
    if (allowed) {
      StatusChange change = attempt(from, to);
      assertThat(change.from()).isEqualTo(from);
      assertThat(change.to()).isEqualTo(to);
      assertThat(change.at()).isEqualTo(AT);
      assertThat(change.changedBy()).isEqualTo(USER);
    } else {
      assertThatThrownBy(() -> attempt(from, to))
          .isInstanceOf(IllegalStatusTransitionException.class)
          .hasMessage("A job that is " + from.label() + " cannot move to " + to.label());
    }
  }

  @Test
  void advanceCannotBeUsedToCancelOrReopen() {
    assertThatThrownBy(() -> StatusChange.advance(APPROVED, CANCELLED, AT, USER))
        .isInstanceOf(IllegalStatusTransitionException.class);
    assertThatThrownBy(() -> StatusChange.advance(CANCELLED, PENDING_SURVEY, AT, USER))
        .isInstanceOf(IllegalStatusTransitionException.class);
  }

  @Test
  void advanceReasonIsOptionalAndTrimmed() {
    assertThat(StatusChange.advance(PENDING_SURVEY, APPROVED, AT, USER).reason()).isEmpty();
    assertThat(StatusChange.advance(PENDING_SURVEY, APPROVED, "  site ok ", AT, USER).reason())
        .isEqualTo("site ok");
  }

  @ParameterizedTest
  @EnumSource(
      value = JobStatus.class,
      names = {"PENDING_SURVEY", "APPROVED", "IN_PROGRESS"})
  void cancelRequiresReason(JobStatus from) {
    for (String blank : new String[] {null, "", "   "}) {
      assertThatThrownBy(() -> StatusChange.cancel(from, blank, AT, USER))
          .isInstanceOfSatisfying(
              ValidationException.class, e -> assertThat(e.messageFor("reason")).isPresent());
    }
  }

  @Test
  void reopenRequiresReason() {
    assertThatThrownBy(() -> StatusChange.reopen(CANCELLED, " ", AT, USER))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("reason")).isPresent());
  }

  @ParameterizedTest
  @EnumSource(value = JobStatus.class, names = "CANCELLED", mode = EnumSource.Mode.EXCLUDE)
  void reopenOnlyFromCancelled(JobStatus from) {
    assertThatThrownBy(() -> StatusChange.reopen(from, "reason", AT, USER))
        .isInstanceOf(IllegalStatusTransitionException.class);
  }

  @Test
  void overlongReasonIsRejected() {
    String reason = "x".repeat(StatusChange.MAX_REASON + 1);
    assertThatThrownBy(() -> StatusChange.cancel(APPROVED, reason, AT, USER))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void nextFollowsTheForwardChain() {
    assertThat(PENDING_SURVEY.next()).contains(APPROVED);
    assertThat(APPROVED.next()).contains(IN_PROGRESS);
    assertThat(IN_PROGRESS.next()).contains(COMPLETED);
    assertThat(COMPLETED.next()).isEmpty();
    assertThat(CANCELLED.next()).isEmpty();
  }

  @Test
  void onlyPendingSurveyQuotesAreEditable() {
    assertThat(Arrays.stream(JobStatus.values()).filter(JobStatus::isQuoteEditable))
        .containsExactly(PENDING_SURVEY);
  }

  @Test
  void everyStateExceptCancelledAcceptsPayments() {
    assertThat(Arrays.stream(JobStatus.values()).filter(s -> !s.acceptsPayments()))
        .containsExactly(CANCELLED);
  }

  @Test
  void labelsAreHumanReadable() {
    assertThat(IN_PROGRESS.label()).isEqualTo("In progress");
    assertThat(PENDING_SURVEY).hasToString("Pending survey");
  }
}
