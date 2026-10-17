package com.uzima.persistence;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.domain.client.EmailAddress;
import com.uzima.domain.client.GpsLocation;
import com.uzima.domain.client.PhoneNumber;
import com.uzima.service.ClientRepository;
import com.uzima.service.ClientSearch;
import com.uzima.service.ClientSummary;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Clients table. Optional email and coordinates are stored as NULL. */
public final class JdbcClientRepository implements ClientRepository {

  private static final String COLUMNS =
      "id, code, name, category, phone, email, county, address, latitude, longitude, active,"
          + " created_at";

  private final Jdbc jdbc;

  public JdbcClientRepository(Jdbc jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public Client insert(Client client, long createdBy) {
    ClientDetails d = client.details();
    long id =
        jdbc.insert(
            "INSERT INTO clients (code, name, category, phone, email, county, address, latitude,"
                + " longitude, active, created_at, created_by)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            client.code().value(),
            d.name(),
            d.category(),
            d.phone().value(),
            d.email().map(EmailAddress::value).orElse(null),
            d.county(),
            d.address(),
            d.location().map(GpsLocation::latitude).orElse(null),
            d.location().map(GpsLocation::longitude).orElse(null),
            client.active(),
            client.createdAt(),
            createdBy);
    return new Client(id, client.code(), d, client.active(), client.createdAt());
  }

  @Override
  public void update(Client client) {
    ClientDetails d = client.details();
    int rows =
        jdbc.update(
            "UPDATE clients SET name = ?, category = ?, phone = ?, email = ?, county = ?,"
                + " address = ?, latitude = ?, longitude = ?, active = ? WHERE id = ?",
            d.name(),
            d.category(),
            d.phone().value(),
            d.email().map(EmailAddress::value).orElse(null),
            d.county(),
            d.address(),
            d.location().map(GpsLocation::latitude).orElse(null),
            d.location().map(GpsLocation::longitude).orElse(null),
            client.active(),
            client.id());
    if (rows != 1) {
      throw new IllegalStateException("No client with id " + client.id());
    }
  }

  @Override
  public Optional<Client> findById(long id) {
    return jdbc.queryOne(
        "SELECT " + COLUMNS + " FROM clients WHERE id = ?", JdbcClientRepository::map, id);
  }

  @Override
  public Optional<Client> findByCode(ClientCode code) {
    return jdbc.queryOne(
        "SELECT " + COLUMNS + " FROM clients WHERE code = ?",
        JdbcClientRepository::map,
        code.value());
  }

  @Override
  public List<ClientSummary> search(ClientSearch s) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT id, code, name, phone, category, county, active FROM clients WHERE 1 = 1");
    List<Object> params = new ArrayList<>();
    s.text()
        .map(String::strip)
        .filter(t -> !t.isEmpty())
        .ifPresent(
            t -> {
              String like = "%" + JdbcAuditRepository.escapeLike(t.toLowerCase()) + "%";
              sql.append(
                  " AND (LOWER(code) LIKE ? ESCAPE '!' OR LOWER(name) LIKE ? ESCAPE '!'"
                      + " OR phone LIKE ? ESCAPE '!')");
              params.add(like);
              params.add(like);
              params.add(phoneLike(t, like));
            });
    s.category()
        .ifPresent(
            c -> {
              sql.append(" AND category = ?");
              params.add(c);
            });
    s.county()
        .ifPresent(
            c -> {
              sql.append(" AND county = ?");
              params.add(c);
            });
    s.active()
        .ifPresent(
            a -> {
              sql.append(" AND active = ?");
              params.add(a);
            });
    sql.append(" ORDER BY name, id LIMIT ?");
    params.add(Math.max(1, s.limit()));
    return jdbc.query(
        sql.toString(),
        rs ->
            new ClientSummary(
                rs.getLong("id"),
                ClientCode.parse(rs.getString("code")),
                rs.getString("name"),
                new PhoneNumber(rs.getString("phone")),
                Jdbc.enumValue(rs, "category", ClientCategory.class),
                Jdbc.enumValue(rs, "county", County.class),
                rs.getBoolean("active")),
        params.toArray());
  }

  /**
   * Lets "0712 345" find "+254712345678": a search that looks like a local number is matched
   * against the stored E.164 form without its leading zero.
   */
  private static String phoneLike(String text, String fallback) {
    String digits = text.replaceAll("[\\s\\-()]", "");
    if (digits.matches("0\\d{2,9}")) {
      return "%" + digits.substring(1) + "%";
    }
    return fallback;
  }

  @Override
  public boolean hasJobs(long clientId) {
    return jdbc.queryOne(
            "SELECT COUNT(*) FROM jobs WHERE client_id = ?", rs -> rs.getLong(1) > 0, clientId)
        .orElse(false);
  }

  @Override
  public int countCreatedBetween(Instant from, Instant to) {
    return jdbc.queryOne(
            "SELECT COUNT(*) FROM clients WHERE created_at >= ? AND created_at < ?",
            rs -> rs.getInt(1),
            from,
            to)
        .orElse(0);
  }

  private static Client map(ResultSet rs) throws SQLException {
    BigDecimal lat = rs.getBigDecimal("latitude");
    BigDecimal lon = rs.getBigDecimal("longitude");
    String email = rs.getString("email");
    ClientDetails details =
        new ClientDetails(
            rs.getString("name"),
            Jdbc.enumValue(rs, "category", ClientCategory.class),
            new PhoneNumber(rs.getString("phone")),
            Optional.ofNullable(email).map(EmailAddress::new),
            Jdbc.enumValue(rs, "county", County.class),
            rs.getString("address"),
            lat == null || lon == null ? Optional.empty() : Optional.of(new GpsLocation(lat, lon)));
    return new Client(
        rs.getLong("id"),
        ClientCode.parse(rs.getString("code")),
        details,
        rs.getBoolean("active"),
        Jdbc.instant(rs, "created_at"));
  }
}
