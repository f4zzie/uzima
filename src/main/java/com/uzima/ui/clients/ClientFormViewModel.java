package com.uzima.ui.clients;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.domain.client.GpsLocation;
import com.uzima.domain.client.PhoneNumber;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.ui.Backend;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The register / edit client form. Inputs are plain text; {@link ClientDetails#parse} in the
 * service call validates them, and each {@code FieldError} lands under its field. Coordinates
 * outside Kenya produce a warning, never an error.
 */
public final class ClientFormViewModel {

  private static final Logger log = LoggerFactory.getLogger(ClientFormViewModel.class);

  /** Field names, as used by {@link com.uzima.domain.common.FieldError}. */
  static final List<String> FIELDS =
      List.of("name", "category", "phone", "email", "county", "address", "latitude", "longitude");

  static final String OUTSIDE_KENYA =
      "This point is outside Kenya. Check the coordinates; you can still save.";
  static final String SAVE_FAILED =
      "Uzima couldn't save the client just now. Try again; if it keeps happening, the details are"
          + " in the log folder.";

  private final Backend backend;
  private final Optional<Client> existing;

  private final StringProperty name = new SimpleStringProperty(this, "name", "");
  private final ObjectProperty<ClientCategory> category =
      new SimpleObjectProperty<>(this, "category");
  private final StringProperty phone = new SimpleStringProperty(this, "phone", "");
  private final StringProperty email = new SimpleStringProperty(this, "email", "");
  private final ObjectProperty<County> county = new SimpleObjectProperty<>(this, "county");
  private final StringProperty address = new SimpleStringProperty(this, "address", "");
  private final StringProperty latitude = new SimpleStringProperty(this, "latitude", "");
  private final StringProperty longitude = new SimpleStringProperty(this, "longitude", "");
  private final ReadOnlyStringWrapper phoneHelp = new ReadOnlyStringWrapper(this, "phoneHelp");
  private final ReadOnlyStringWrapper gpsWarning =
      new ReadOnlyStringWrapper(this, "gpsWarning", "");
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");
  private final Map<String, StringProperty> errors = new LinkedHashMap<>();

  /** A form for a new client. */
  public ClientFormViewModel(Backend backend) {
    this(backend, Optional.empty());
  }

  /** A form for editing {@code client}, filled in with its details. */
  public ClientFormViewModel(Backend backend, Client client) {
    this(backend, Optional.of(client));
  }

  private ClientFormViewModel(Backend backend, Optional<Client> existing) {
    this.backend = backend;
    this.existing = existing;
    for (String field : FIELDS) {
      errors.put(field, new SimpleStringProperty(this, field + "Error", ""));
    }
    existing.ifPresent(this::fill);
    phoneHelp.set(defaultPhoneHelp());
    latitude.addListener((obs, old, now) -> updateGpsWarning());
    longitude.addListener((obs, old, now) -> updateGpsWarning());
    updateGpsWarning();
  }

  public boolean isEdit() {
    return existing.isPresent();
  }

  public Optional<Client> existing() {
    return existing;
  }

  /**
   * Shows the number as it will be saved, once the user leaves the phone field; a number that
   * doesn't parse is left for the save to report.
   */
  public void phoneEdited() {
    try {
      PhoneNumber parsed = PhoneNumber.parse(phone.get());
      phone.set(parsed.formatLocal());
      phoneHelp.set("Saved as " + parsed.value());
    } catch (ValidationException notYet) {
      phoneHelp.set(defaultPhoneHelp());
    }
  }

  /** Validates and saves in the background; {@code onSaved} runs on the FX thread. */
  public void save(Consumer<Client> onSaved) {
    if (busy.get()) {
      return;
    }
    errors.values().forEach(e -> e.set(""));
    failure.set("");
    busy.set(true);
    String n = name.get();
    // In edit mode this follows the client's category, including a change made while the form is
    // open (see CategoryChangeDialog), so the update never looks like a category change.
    ClientCategory c = category.get();
    String p = phone.get();
    String e = email.get();
    County k = county.get();
    String a = address.get();
    String lat = latitude.get();
    String lon = longitude.get();
    Optional<Long> id = existing.map(Client::id);
    backend.run(
        s -> {
          ClientDetails details = ClientDetails.parse(n, c, p, e, k, a, lat, lon);
          return id.isPresent()
              ? s.clients().update(id.get(), details)
              : s.clients().register(details);
        },
        saved -> {
          busy.set(false);
          onSaved.accept(saved);
        },
        this::fail);
  }

  /** Sets the messages first, then clears busy, so listeners on busy see the final errors. */
  private void fail(Throwable error) {
    switch (error) {
      case ValidationException invalid ->
          invalid
              .errors()
              .forEach(
                  fe -> {
                    StringProperty target = errors.get(fe.field());
                    if (target == null) {
                      failure.set(fe.message());
                    } else if (target.get().isEmpty()) {
                      target.set(fe.message());
                    }
                  });
      case DomainException rule -> failure.set(rule.getMessage());
      default -> {
        log.error("Saving a client failed", error);
        failure.set(SAVE_FAILED);
      }
    }
    busy.set(false);
  }

  private void fill(Client client) {
    ClientDetails d = client.details();
    name.set(d.name());
    category.set(d.category());
    phone.set(d.phone().formatLocal());
    email.set(d.email().map(Object::toString).orElse(""));
    county.set(d.county());
    address.set(d.address());
    d.location()
        .ifPresent(
            l -> {
              latitude.set(l.latitude().stripTrailingZeros().toPlainString());
              longitude.set(l.longitude().stripTrailingZeros().toPlainString());
            });
  }

  private String defaultPhoneHelp() {
    return existing
        .map(c -> "Saved as " + c.details().phone().value())
        .orElse("Kenyan mobile, e.g. 0712 345 678. Saved as +254…");
  }

  private void updateGpsWarning() {
    String lat = latitude.get();
    String lon = longitude.get();
    if (lat == null || lon == null || lat.isBlank() || lon.isBlank()) {
      gpsWarning.set("");
      return;
    }
    try {
      gpsWarning.set(GpsLocation.parse(lat, lon).isWithinKenya() ? "" : OUTSIDE_KENYA);
    } catch (ValidationException notCoordinatesYet) {
      gpsWarning.set("");
    }
  }

  public StringProperty nameProperty() {
    return name;
  }

  public ObjectProperty<ClientCategory> categoryProperty() {
    return category;
  }

  public StringProperty phoneProperty() {
    return phone;
  }

  public StringProperty emailProperty() {
    return email;
  }

  public ObjectProperty<County> countyProperty() {
    return county;
  }

  public StringProperty addressProperty() {
    return address;
  }

  public StringProperty latitudeProperty() {
    return latitude;
  }

  public StringProperty longitudeProperty() {
    return longitude;
  }

  public ReadOnlyStringProperty phoneHelpProperty() {
    return phoneHelp.getReadOnlyProperty();
  }

  public ReadOnlyStringProperty gpsWarningProperty() {
    return gpsWarning.getReadOnlyProperty();
  }

  /** The inline error for {@code field} (one of {@link #FIELDS}); empty when there is none. */
  public StringProperty errorProperty(String field) {
    StringProperty p = errors.get(field);
    if (p == null) {
      throw new IllegalArgumentException("Unknown field " + field);
    }
    return p;
  }

  /** A form-level message for problems that belong to no field. */
  public StringProperty failureProperty() {
    return failure;
  }

  public BooleanProperty busyProperty() {
    return busy;
  }
}
