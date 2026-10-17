package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.uzima.domain.user.PasswordPolicy;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TemporaryPasswordsTest {

  @Test
  void generatesPolicyCompliantPasswordsFromTheUnambiguousAlphabet() {
    TemporaryPasswords generator = new TemporaryPasswords();
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 200; i++) {
      String p = generator.generate();
      assertThat(p).hasSize(TemporaryPasswords.LENGTH);
      assertThat(p.chars()).allMatch(c -> TemporaryPasswords.ALPHABET.indexOf(c) >= 0);
      PasswordPolicy.validate(p);
      seen.add(p);
    }
    assertThat(seen).hasSize(200);
    assertThat(TemporaryPasswords.ALPHABET).doesNotContain("0", "O", "1", "l", "I");
  }
}
