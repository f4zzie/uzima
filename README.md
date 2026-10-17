# Uzima

Desktop app for Uzima Borehole Drilling Company: clients, quotes, jobs, payments, invoices and reports.
Java 21 and JavaFX 21, with an embedded H2 database by default and MySQL 8 as an option.

> **Status:** under active rebuild. This README covers only what exists today and grows with each phase.

## Requirements

- JDK 21 (a full JDK, not just a JRE)
- No Maven install needed: use the committed wrapper (`./mvnw`, or `mvnw.cmd` on Windows)

## Quick start

```bash
./mvnw javafx:run
```

On first run the app creates its data directory, then creates and migrates the embedded H2 database inside it:

| OS | App data directory |
|---|---|
| Linux | `$XDG_DATA_HOME/uzima` (default `~/.local/share/uzima`) |
| Windows | `%APPDATA%\Uzima` |
| macOS | `~/Library/Application Support/Uzima` |

Set `UZIMA_HOME` to use a different directory, for example a throwaway one for testing.
Logs go to `<app data>/logs/uzima.log` and roll daily.

## Configuration

Later sources override earlier ones:

1. `src/main/resources/application.properties` (defaults, committed)
2. `<app data>/uzima.properties` (optional; see `uzima.properties.example`)
3. Environment variables `UZIMA_DB_URL`, `UZIMA_DB_USER` and `UZIMA_DB_PASSWORD`

The repository contains no credentials.

## Build and test

```bash
./mvnw verify          # compile, test, check formatting, coverage report
./mvnw spotless:apply  # format code (google-java-format)
```

The coverage report is written to `target/site/jacoco/index.html`.

## Further reading

- [Design decisions](docs/decisions.md): what was decided beyond the specification, why, and what was rejected.
