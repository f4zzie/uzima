package com.uzima;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Enforces the dependency rules from the architecture: the domain and pricing layers are pure Java
 * (no UI, JDBC or outer-layer imports) and money code never uses binary floating point.
 */
class ArchitectureTest {

  private static final Path SOURCES = Path.of("src/main/java/com/uzima");

  private static final List<String> FORBIDDEN_IN_CORE =
      List.of(
          "javafx.",
          "java.sql.",
          "javax.sql.",
          "com.uzima.persistence",
          "com.uzima.ui",
          "com.uzima.service",
          "com.uzima.app",
          "com.uzima.report");

  private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.*]+)");
  private static final Pattern FLOATING_POINT =
      Pattern.compile("\\b(double|float|Double|Float)\\b");
  private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
  private static final Pattern LINE_COMMENT = Pattern.compile("//[^\\n]*");
  private static final Pattern STRING_LITERAL = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"");

  @Test
  void domainAndPricingImportNoOuterLayers() {
    List<String> violations = new ArrayList<>();
    for (Path file : javaFiles("domain", "pricing")) {
      for (String imported : imports(file)) {
        for (String forbidden : FORBIDDEN_IN_CORE) {
          if (imported.startsWith(forbidden)) {
            violations.add(SOURCES.relativize(file) + " imports " + imported);
          }
        }
      }
    }
    assertThat(violations).isEmpty();
  }

  @Test
  void domainDoesNotDependOnPricing() {
    List<String> violations = new ArrayList<>();
    for (Path file : javaFiles("domain")) {
      for (String imported : imports(file)) {
        if (imported.startsWith("com.uzima.pricing")) {
          violations.add(SOURCES.relativize(file) + " imports " + imported);
        }
      }
    }
    assertThat(violations).isEmpty();
  }

  @Test
  void moneyAndPricingNeverUseFloatingPoint() {
    List<String> violations = new ArrayList<>();
    for (Path file : javaFiles("domain/money", "pricing")) {
      String code = codeOnly(read(file));
      String[] lines = code.split("\n", -1);
      for (int i = 0; i < lines.length; i++) {
        if (FLOATING_POINT.matcher(lines[i]).find()) {
          violations.add(SOURCES.relativize(file) + ":" + (i + 1) + " " + lines[i].strip());
        }
      }
    }
    assertThat(violations).isEmpty();
  }

  @Test
  void scannerSeesTheDomainSources() {
    // Guards against the rules above passing vacuously if the source root moves.
    assertThat(javaFiles("domain")).isNotEmpty();
  }

  @Test
  void codeOnlyStripsCommentsAndStrings() {
    String code =
        """
        /** a double in javadoc */
        int x = 1; // float here
        String s = "Double quoted";
        double y;
        """;
    String stripped = codeOnly(code);
    assertThat(stripped).doesNotContain("javadoc", "float here", "Double quoted");
    assertThat(stripped.split("\n", -1)).hasSize(5);
    assertThat(FLOATING_POINT.matcher(stripped).results().count()).isEqualTo(1);
  }

  private static List<Path> javaFiles(String... packages) {
    List<Path> files = new ArrayList<>();
    for (String pkg : packages) {
      Path dir = SOURCES.resolve(pkg);
      if (!Files.isDirectory(dir)) {
        continue;
      }
      try (Stream<Path> walk = Files.walk(dir)) {
        walk.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return files;
  }

  private static List<String> imports(Path file) {
    List<String> result = new ArrayList<>();
    for (String line : read(file).split("\n")) {
      var m = IMPORT.matcher(line);
      if (m.find()) {
        result.add(m.group(2));
      }
    }
    return result;
  }

  /** Blanks out comments and string literals, keeping line breaks so line numbers stay right. */
  private static String codeOnly(String source) {
    String result = blank(BLOCK_COMMENT, source);
    result = blank(STRING_LITERAL, result);
    return blank(LINE_COMMENT, result);
  }

  private static String blank(Pattern pattern, String text) {
    return pattern.matcher(text).replaceAll(m -> m.group().replaceAll("[^\\n]", " "));
  }

  private static String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
