package com.uzima.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Keeps the design system honest: literal colours only in the token block of theme.css, lengths
 * only from the documented scales, and no inline styles in FXML or Java.
 */
class ThemeTokensTest {

  private static final Path THEME = Path.of("src/main/resources/com/uzima/ui/css/theme.css");
  private static final Path RESOURCES = Path.of("src/main/resources/com/uzima/ui");
  private static final Path UI_SOURCES = Path.of("src/main/java/com/uzima/ui");

  private static final Pattern COLOUR_LITERAL =
      Pattern.compile(
          "#[0-9a-fA-F]{3,8}\\b|\\b(rgb|rgba|hsb|hsba)\\s*\\(|:\\s*(white|black|red|green|blue|gray|grey)\\b");
  private static final Pattern DECLARATION = Pattern.compile("(-fx-[a-z-]+)\\s*:\\s*([^;]+);");
  private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");

  private static final Set<Double> FONT_SIZES = set(11, 12, 13, 14, 16, 18, 20, 24, 30);
  private static final Set<Double> SPACING =
      set(0, 1, 2, 3, 4, 6, 8, 10, 12, 16, 20, 24, 32, 40, 48);
  private static final Set<Double> RADII = set(0, 4, 6, 8, 12, 999);

  @Test
  void literalColoursOnlyAppearInTheTokenBlock() throws IOException {
    String css = withoutComments(Files.readString(THEME));
    String outsideTokens = css.replace(tokenBlock(css), "");
    assertThat(matches(COLOUR_LITERAL, outsideTokens)).isEmpty();
  }

  @Test
  void lengthsComeFromTheScales() throws IOException {
    String css = withoutComments(Files.readString(THEME));
    List<String> violations = new ArrayList<>();
    Matcher m = DECLARATION.matcher(css.replace(tokenBlock(css), ""));
    while (m.find()) {
      String property = m.group(1);
      Set<Double> scale = scaleFor(property);
      if (scale == null) {
        continue;
      }
      Matcher n = NUMBER.matcher(m.group(2));
      while (n.find()) {
        double value = Math.abs(Double.parseDouble(n.group()));
        if (!scale.contains(value)) {
          violations.add(property + ": " + m.group(2).trim());
        }
      }
    }
    assertThat(violations).isEmpty();
  }

  @Test
  void fxmlHasNoInlineStylesOrColours() throws IOException {
    try (Stream<Path> files = Files.walk(RESOURCES)) {
      List<String> offenders =
          files
              .filter(p -> p.toString().endsWith(".fxml"))
              .filter(p -> containsAny(p, "style=", "textFill=", "fill=\"#", "Color."))
              .map(Path::toString)
              .toList();
      assertThat(offenders).isEmpty();
    }
  }

  @Test
  void uiCodeDoesNotSetInlineStylesOrColours() throws IOException {
    try (Stream<Path> files = Files.walk(UI_SOURCES)) {
      List<String> offenders =
          files
              .filter(p -> p.toString().endsWith(".java"))
              .filter(
                  p -> containsAny(p, "setStyle(", "Color.web(", "Color.rgb(", "Color.valueOf("))
              .map(Path::toString)
              .toList();
      assertThat(offenders).isEmpty();
    }
  }

  @Test
  void onlyOneStylesheetExists() throws IOException {
    try (Stream<Path> files = Files.walk(RESOURCES)) {
      assertThat(files.filter(p -> p.toString().endsWith(".css")).toList())
          .containsExactly(RESOURCES.resolve("css/theme.css"));
    }
  }

  private static Set<Double> scaleFor(String property) {
    if (property.equals("-fx-font-size")) {
      return FONT_SIZES;
    }
    if (property.endsWith("-radius")) {
      return RADII;
    }
    if (property.equals("-fx-padding")
        || property.equals("-fx-spacing")
        || property.equals("-fx-hgap")
        || property.equals("-fx-vgap")
        || property.endsWith("-insets")
        || property.equals("-fx-border-width")) {
      return SPACING;
    }
    return null;
  }

  private static String tokenBlock(String css) {
    int start = css.indexOf(".root {");
    return css.substring(start, css.indexOf('}', start) + 1);
  }

  private static String withoutComments(String css) {
    return css.replaceAll("(?s)/\\*.*?\\*/", "");
  }

  private static List<String> matches(Pattern pattern, String text) {
    List<String> found = new ArrayList<>();
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      found.add(m.group());
    }
    return found;
  }

  private static boolean containsAny(Path file, String... needles) {
    try {
      String text = Files.readString(file);
      return Stream.of(needles).anyMatch(text::contains);
    } catch (IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }

  private static Set<Double> set(double... values) {
    return java.util.Arrays.stream(values).boxed().collect(java.util.stream.Collectors.toSet());
  }
}
