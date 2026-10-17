package com.uzima.domain.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.FieldError;
import com.uzima.domain.common.ValidationException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClientTest {

  private static final Instant CREATED = Instant.parse("2026-01-15T07:30:00Z");

  private static ClientDetails valid(ClientCategory category) {
    return ClientDetails.parse(
        "  Wanjiku Farms Ltd ",
        category,
        "0712 345 678",
        "Info@WanjikuFarms.co.ke",
        County.KIAMBU,
        " Plot 12, Limuru Road ",
        "-1.1714",
        "36.8356");
  }

  @Test
  void parseNormalisesValidInput() {
    ClientDetails d = valid(ClientCategory.COMMERCIAL);
    assertThat(d.name()).isEqualTo("Wanjiku Farms Ltd");
    assertThat(d.category()).isEqualTo(ClientCategory.COMMERCIAL);
    assertThat(d.phone().value()).isEqualTo("+254712345678");
    assertThat(d.email()).map(EmailAddress::value).contains("info@wanjikufarms.co.ke");
    assertThat(d.county()).isEqualTo(County.KIAMBU);
    assertThat(d.address()).isEqualTo("Plot 12, Limuru Road");
    assertThat(d.location()).isPresent();
    assertThat(d.hasLocationOutsideKenya()).isFalse();
  }

  @Test
  void parseReportsEveryInvalidFieldAtOnce() {
    assertThatThrownBy(
            () ->
                ClientDetails.parse(
                    "  ",
                    ClientCategory.DOMESTIC,
                    "12345",
                    "not-an-email",
                    County.NAKURU,
                    "",
                    "",
                    ""))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(FieldError::field)
                    .containsExactlyInAnyOrder("name", "phone", "email"));
  }

  @Test
  void parseRequiresCategoryAndCounty() {
    assertThatThrownBy(
            () -> ClientDetails.parse("A", null, "0712345678", null, null, null, null, null))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(FieldError::field)
                    .containsExactlyInAnyOrder("category", "county"));
  }

  @Test
  void parseRejectsOverlongNameAndAddress() {
    String longName = "x".repeat(ClientDetails.MAX_NAME + 1);
    String longAddress = "y".repeat(ClientDetails.MAX_ADDRESS + 1);
    assertThatThrownBy(
            () ->
                ClientDetails.parse(
                    longName,
                    ClientCategory.DOMESTIC,
                    "0712345678",
                    "",
                    County.NAIROBI,
                    longAddress,
                    "",
                    ""))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(FieldError::field)
                    .containsExactlyInAnyOrder("name", "address"));
  }

  @Test
  void blankOptionalFieldsBecomeEmpty() {
    ClientDetails d =
        ClientDetails.parse(
            "Otieno", ClientCategory.DOMESTIC, "0712345678", "  ", County.KISUMU, null, " ", "");
    assertThat(d.email()).isEmpty();
    assertThat(d.location()).isEmpty();
    assertThat(d.address()).isEmpty();
    assertThat(d.hasLocationOutsideKenya()).isFalse();
  }

  @Test
  void onlyOneCoordinateIsAnError() {
    assertThatThrownBy(
            () ->
                ClientDetails.parse(
                    "A", ClientCategory.DOMESTIC, "0712345678", "", County.KISUMU, "", "-0.1", ""))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("longitude")).isPresent());
    assertThatThrownBy(
            () ->
                ClientDetails.parse(
                    "A", ClientCategory.DOMESTIC, "0712345678", "", County.KISUMU, "", "", "34.7"))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("latitude")).isPresent());
  }

  @Test
  void invalidCoordinatesAreReported() {
    assertThatThrownBy(
            () ->
                ClientDetails.parse(
                    "A", ClientCategory.DOMESTIC, "0712345678", "", County.KISUMU, "", "95", "x"))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors()).extracting(FieldError::field).containsExactly("longitude"));
  }

  @Test
  void locationOutsideKenyaIsAWarningNotAnError() {
    ClientDetails d =
        ClientDetails.parse(
            "Border Site",
            ClientCategory.INDUSTRIAL,
            "0712345678",
            "",
            County.BUSIA,
            "",
            "0.3476",
            "32.5825");
    assertThat(d.hasLocationOutsideKenya()).isTrue();
  }

  @Test
  void constructorDefaultsNullOptionals() {
    ClientDetails d =
        new ClientDetails(
            "A",
            ClientCategory.DOMESTIC,
            PhoneNumber.parse("0712345678"),
            null,
            County.NAIROBI,
            null,
            null);
    assertThat(d.email()).isEqualTo(Optional.empty());
    assertThat(d.location()).isEmpty();
    assertThat(d.address()).isEmpty();
  }

  @Test
  void clientEditDeactivateReactivate() {
    ClientDetails details = valid(ClientCategory.DOMESTIC);
    Client client =
        new Client(7, new ClientCode(ClientCategory.DOMESTIC, 42), details, true, CREATED);
    assertThat(client.category()).isEqualTo(ClientCategory.DOMESTIC);
    assertThat(client.name()).isEqualTo("Wanjiku Farms Ltd");

    ClientDetails renamed =
        ClientDetails.parse(
            "Wanjiku Homes", ClientCategory.DOMESTIC, "0722000111", "", County.KIAMBU, "", "", "");
    Client edited = client.withDetails(renamed);
    assertThat(edited.name()).isEqualTo("Wanjiku Homes");
    assertThat(edited.id()).isEqualTo(7);
    assertThat(edited.code()).isEqualTo(client.code());
    assertThat(edited.createdAt()).isEqualTo(CREATED);

    Client inactive = edited.deactivated();
    assertThat(inactive.active()).isFalse();
    assertThat(inactive.reactivated().active()).isTrue();
  }

  @Test
  void categoryCannotChangeAfterRegistration() {
    Client client =
        new Client(
            1,
            new ClientCode(ClientCategory.DOMESTIC, 1),
            valid(ClientCategory.DOMESTIC),
            true,
            CREATED);
    assertThatThrownBy(() -> client.withDetails(valid(ClientCategory.INDUSTRIAL)))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("category")).isPresent());
  }

  @Test
  void codeCategoryMustMatchDetails() {
    assertThatThrownBy(
            () ->
                new Client(
                    1,
                    new ClientCode(ClientCategory.INDUSTRIAL, 1),
                    valid(ClientCategory.DOMESTIC),
                    true,
                    CREATED))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Client(1, null, valid(ClientCategory.DOMESTIC), true, CREATED))
        .isInstanceOf(NullPointerException.class);
  }
}
