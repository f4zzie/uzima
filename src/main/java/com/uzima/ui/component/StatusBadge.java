package com.uzima.ui.component;

import com.uzima.domain.job.JobStatus;
import com.uzima.domain.payment.PaymentStatus;
import java.util.Locale;
import javafx.scene.control.Label;

/** A small pill showing a status in words and a colour tone. */
public class StatusBadge extends Label {

  /** Colour schemes; each maps to a {@code badge-<tone>} style class in theme.css. */
  public enum Tone {
    NEUTRAL,
    INFO,
    SUCCESS,
    WARNING,
    DANGER,
    ACCENT;

    String styleClass() {
      return "badge-" + name().toLowerCase(Locale.ROOT);
    }
  }

  private Tone tone;

  public StatusBadge(String text, Tone tone) {
    getStyleClass().add("badge");
    setMinWidth(USE_PREF_SIZE);
    setText(text, tone);
  }

  public static StatusBadge of(JobStatus status) {
    return new StatusBadge(status.label(), toneOf(status));
  }

  public static StatusBadge of(PaymentStatus status) {
    return new StatusBadge(status.label(), toneOf(status));
  }

  public static Tone toneOf(JobStatus status) {
    return switch (status) {
      case PENDING_SURVEY -> Tone.NEUTRAL;
      case APPROVED -> Tone.INFO;
      case IN_PROGRESS -> Tone.ACCENT;
      case COMPLETED -> Tone.SUCCESS;
      case CANCELLED -> Tone.DANGER;
    };
  }

  public static Tone toneOf(PaymentStatus status) {
    return switch (status) {
      case NOT_PAID -> Tone.DANGER;
      case PARTIALLY_PAID -> Tone.WARNING;
      case PAID -> Tone.SUCCESS;
    };
  }

  public Tone getTone() {
    return tone;
  }

  /** Changes the text and tone together, e.g. when a job's status changes in place. */
  public final void setText(String text, Tone newTone) {
    if (tone != null) {
      getStyleClass().remove(tone.styleClass());
    }
    tone = newTone;
    getStyleClass().add(newTone.styleClass());
    setText(text);
    setAccessibleText("Status: " + text);
  }
}
