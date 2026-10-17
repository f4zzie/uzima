package com.uzima.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JdbcUrlsTest {

  @Test
  void leavesUrlsWithoutCredentialsAlone() {
    String url = "jdbc:h2:file:/data/uzima;MODE=MySQL";
    assertThat(JdbcUrls.redact(url)).isEqualTo(url);
  }

  @Test
  void redactsQueryStylePassword() {
    assertThat(JdbcUrls.redact("jdbc:mysql://db/uzima?user=u&password=s3cret&useSSL=true"))
        .isEqualTo("jdbc:mysql://db/uzima?user=u&password=***&useSSL=true");
  }

  @Test
  void redactsSemicolonStylePassword() {
    assertThat(JdbcUrls.redact("jdbc:h2:file:/x;USER=u;PASSWORD=s3cret;MODE=MySQL"))
        .isEqualTo("jdbc:h2:file:/x;USER=u;PASSWORD=***;MODE=MySQL");
  }

  @Test
  void redactsUserInfo() {
    assertThat(JdbcUrls.redact("jdbc:mysql://admin:s3cret@db:3306/uzima"))
        .isEqualTo("jdbc:mysql://admin:***@db:3306/uzima");
  }

  @Test
  void databaseConfigToStringNeverShowsPassword() {
    DatabaseConfig config =
        new DatabaseConfig("jdbc:mysql://db/uzima?password=inurl", "u", "s3cret", 2);
    assertThat(config.toString()).doesNotContain("s3cret").doesNotContain("inurl");
  }
}
