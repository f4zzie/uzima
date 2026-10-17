package com.uzima.persistence;

import java.util.regex.Pattern;

/** Helpers for making JDBC URLs safe to log. */
public final class JdbcUrls {

  /** {@code //user:secret@host} style credentials. */
  private static final Pattern USER_INFO = Pattern.compile("//([^/@:?;]+):[^/@]*@");

  /** {@code password=...} in either {@code ?a=b&c=d} or {@code ;a=b;c=d} parameter syntax. */
  private static final Pattern PASSWORD_PARAM =
      Pattern.compile("(?i)([?&;](?:password|pwd)=)[^&;]*");

  private JdbcUrls() {}

  public static String redact(String url) {
    if (url == null) {
      return null;
    }
    String result = USER_INFO.matcher(url).replaceAll("//$1:***@");
    return PASSWORD_PARAM.matcher(result).replaceAll("$1***");
  }
}
