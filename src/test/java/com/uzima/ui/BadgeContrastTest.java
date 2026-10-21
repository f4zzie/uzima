package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.job.JobStatus;
import com.uzima.domain.payment.PaymentStatus;
import com.uzima.ui.component.StatusBadge;
import com.uzima.ui.component.StatusBadge.Tone;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Every badge's text must have a WCAG contrast ratio of at least 4.5:1 against its background. The
 * colours are read from the token block and the {@code .badge-<tone>} rules in theme.css, so a
 * token change that breaks contrast fails here.
 */
class BadgeContrastTest {

  private static final Path THEME = Path.of("src/main/resources/com/uzima/ui/css/theme.css");
  private static final Pattern TOKEN = Pattern.compile("(-uz-[a-z-]+):\\s*(#[0-9a-fA-F]{6});");

  private static final Map<String, String> tokens = new HashMap<>();
  private static String css;

  @BeforeAll
  static void readTheme() throws IOException {
    css = Files.readString(THEME);
    Matcher m =
        TOKEN.matcher(
            css.substring(css.indexOf(".root {"), css.indexOf('}', css.indexOf(".root {"))));
    while (m.find()) {
      tokens.put(m.group(1), m.group(2));
    }
  }

  @ParameterizedTest
  @EnumSource(Tone.class)
  void badgeTextIsReadable(Tone tone) {
    String rule = rule(".badge-" + tone.name().toLowerCase(Locale.ROOT));
    String background = colour(rule, "-fx-background-color");
    String text = colour(rule, "-fx-text-fill");
    assertThat(contrast(background, text))
        .as("%s text on background", tone)
        .isGreaterThanOrEqualTo(4.5);
  }

  @Test
  void redIsOnlyForCancellation() {
    for (JobStatus s : JobStatus.values()) {
      assertThat(StatusBadge.toneOf(s) == Tone.DANGER)
          .as(s.name())
          .isEqualTo(s == JobStatus.CANCELLED);
    }
    for (PaymentStatus s : PaymentStatus.values()) {
      assertThat(StatusBadge.toneOf(s)).as(s.name()).isNotEqualTo(Tone.DANGER);
    }
  }

  @Test
  void approvedAndInProgressLookDifferent() {
    assertThat(StatusBadge.toneOf(JobStatus.APPROVED))
        .isNotEqualTo(StatusBadge.toneOf(JobStatus.IN_PROGRESS));
    assertThat(StatusBadge.toneOf(JobStatus.IN_PROGRESS)).isEqualTo(Tone.ACCENT);
  }

  private static String rule(String selector) {
    int start = css.indexOf(selector + " {");
    assertThat(start).as("rule %s", selector).isNotNegative();
    return css.substring(start, css.indexOf('}', start));
  }

  private static String colour(String rule, String property) {
    Matcher m = Pattern.compile(property + ":\\s*(-uz-[a-z-]+);").matcher(rule);
    assertThat(m.find()).as(property).isTrue();
    String hex = tokens.get(m.group(1));
    assertThat(hex).as("token %s", m.group(1)).isNotNull();
    return hex;
  }

  static double contrast(String a, String b) {
    double la = luminance(a);
    double lb = luminance(b);
    return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
  }

  private static double luminance(String hex) {
    double[] c = new double[3];
    for (int i = 0; i < 3; i++) {
      double v = Integer.parseInt(hex.substring(1 + 2 * i, 3 + 2 * i), 16) / 255.0;
      c[i] = v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
  }
}
