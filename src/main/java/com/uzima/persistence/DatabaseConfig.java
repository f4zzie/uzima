package com.uzima.persistence;

import java.util.Objects;

/** Connection settings. {@link #toString()} never exposes the password or URL credentials. */
public record DatabaseConfig(String url, String user, String password, int maxPoolSize) {

  public DatabaseConfig {
    Objects.requireNonNull(url, "url");
    if (maxPoolSize < 1) {
      throw new IllegalArgumentException("maxPoolSize must be at least 1");
    }
  }

  public boolean isH2() {
    return url.startsWith("jdbc:h2:");
  }

  @Override
  public String toString() {
    return "DatabaseConfig[url="
        + JdbcUrls.redact(url)
        + ", user="
        + user
        + ", password="
        + (password == null || password.isEmpty() ? "<none>" : "<redacted>")
        + ", maxPoolSize="
        + maxPoolSize
        + "]";
  }
}
