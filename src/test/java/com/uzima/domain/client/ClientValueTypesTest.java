package com.uzima.domain.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClientValueTypesTest {

  @ParameterizedTest
  @CsvSource({
    "INDUSTRIAL, IND, Industrial",
    "COMMERCIAL, COM, Commercial",
    "DOMESTIC, DOM, Domestic"
  })
  void categoryCodes(ClientCategory category, String code, String label) {
    assertThat(category.code()).isEqualTo(code);
    assertThat(category.label()).isEqualTo(label);
    assertThat(ClientCategory.fromCode(code)).isEqualTo(category);
  }

  @Test
  void unknownCategoryCode() {
    assertThatThrownBy(() -> ClientCategory.fromCode("XYZ"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void clientCodeFormat() {
    assertThat(new ClientCode(ClientCategory.DOMESTIC, 42).value()).isEqualTo("UZM-DOM-0042");
    assertThat(new ClientCode(ClientCategory.INDUSTRIAL, 1).toString()).isEqualTo("UZM-IND-0001");
    assertThat(new ClientCode(ClientCategory.COMMERCIAL, 9999).value()).isEqualTo("UZM-COM-9999");
    assertThat(new ClientCode(ClientCategory.COMMERCIAL, 12345).value()).isEqualTo("UZM-COM-12345");
  }

  @ParameterizedTest
  @ValueSource(strings = {"UZM-DOM-0042", "UZM-IND-0001", "UZM-COM-12345", " UZM-DOM-0007 "})
  void clientCodeParseRoundTrips(String text) {
    ClientCode code = ClientCode.parse(text);
    assertThat(code.value()).isEqualTo(text.trim());
    assertThat(ClientCode.parse(code.value())).isEqualTo(code);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"uzm5", "DOM-2025-0001", "UZM-IND-001", "UZM-XYZ-0001", "uzm-dom-0001", ""})
  void clientCodeRejectsLegacyAndMalformedIds(String text) {
    assertThatThrownBy(() -> ClientCode.parse(text))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("clientCode")).isPresent());
  }

  @Test
  void clientCodeRejectsNullAndNonPositive() {
    assertThatThrownBy(() -> ClientCode.parse(null)).isInstanceOf(ValidationException.class);
    assertThatThrownBy(() -> new ClientCode(ClientCategory.DOMESTIC, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClientCode(ClientCategory.DOMESTIC, -1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClientCode(null, 1)).isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @CsvSource({
    "0712345678, +254712345678",
    "0712 345 678, +254712345678",
    "712345678, +254712345678",
    "254712345678, +254712345678",
    "+254 712-345-678, +254712345678",
    "00254712345678, +254712345678",
    "(0712) 345.678, +254712345678",
    "0112345678, +254112345678",
    "+254112345678, +254112345678"
  })
  void phoneNormalisesToE164(String input, String expected) {
    PhoneNumber phone = PhoneNumber.parse(input);
    assertThat(phone.value()).isEqualTo(expected);
    assertThat(phone.toString()).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0201234567",
        "071234567",
        "07123456789",
        "2547123456",
        "07123x5678",
        "+44 7123 456789",
        "812345678",
        "   "
      })
  void phoneRejectsInvalidNumbers(String input) {
    assertThatThrownBy(() -> PhoneNumber.parse(input))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors().getFirst().field()).isEqualTo("phone"));
  }

  @Test
  void phoneRequired() {
    assertThatThrownBy(() -> PhoneNumber.parse(null))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.messageFor("phone")).contains("Phone number is required"));
    assertThatThrownBy(() -> new PhoneNumber("0712345678")).isInstanceOf(ValidationException.class);
  }

  @Test
  void phoneFormatLocal() {
    assertThat(PhoneNumber.parse("+254712345678").formatLocal()).isEqualTo("0712 345 678");
    assertThat(PhoneNumber.parse("0112345678").formatLocal()).isEqualTo("0112 345 678");
  }

  @ParameterizedTest
  @CsvSource({
    "Name.Surname@Example.co.ke, name.surname@example.co.ke",
    "a@b.co, a@b.co",
    "' info+jobs@uzima-drilling.com ', info+jobs@uzima-drilling.com",
    "o'brien@mail.org, o'brien@mail.org"
  })
  void validEmails(String input, String expected) {
    assertThat(EmailAddress.parse(input).value()).isEqualTo(expected);
    assertThat(EmailAddress.parse(input).toString()).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"plain", "@example.com", "a@", "a@b", "a b@c.com", "a@b.c", ""})
  void invalidEmails(String input) {
    assertThatThrownBy(() -> EmailAddress.parse(input))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("email")).isPresent());
  }

  @Test
  void emailNullAndTooLong() {
    assertThatThrownBy(() -> EmailAddress.parse(null)).isInstanceOf(ValidationException.class);
    String longLocal = "a".repeat(250);
    assertThatThrownBy(() -> EmailAddress.parse(longLocal + "@b.com"))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void gpsInsideAndOutsideKenya() {
    GpsLocation nairobi = GpsLocation.parse("-1.2921", "36.8219");
    assertThat(nairobi.isWithinKenya()).isTrue();
    assertThat(nairobi.toString()).isEqualTo("-1.2921, 36.8219");

    GpsLocation kampala = GpsLocation.parse("0.3476", "32.5825");
    assertThat(kampala.isWithinKenya()).isFalse();

    assertThat(GpsLocation.parse("-4.0435", "39.6682").isWithinKenya()).isTrue(); // Mombasa
    assertThat(GpsLocation.parse("3.1166", "35.5966").isWithinKenya()).isTrue(); // Lodwar
    assertThat(GpsLocation.parse("-6.7924", "39.2083").isWithinKenya()).isFalse(); // Dar
  }

  @Test
  void gpsRejectsOutOfRangeCoordinates() {
    assertThatThrownBy(() -> new GpsLocation(new BigDecimal("91"), new BigDecimal("36")))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors()).extracting("field").containsExactly("latitude"));
    assertThatThrownBy(() -> new GpsLocation(new BigDecimal("-1"), new BigDecimal("181")))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors()).extracting("field").containsExactly("longitude"));
    assertThatThrownBy(() -> new GpsLocation(null, null))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting("field")
                    .containsExactly("latitude", "longitude"));
  }

  @Test
  void gpsParseReportsEachBadField() {
    assertThatThrownBy(() -> GpsLocation.parse("north", "east"))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting("field")
                    .containsExactly("latitude", "longitude"));
    assertThatThrownBy(() -> GpsLocation.parse("-1.2", null))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e -> assertThat(e.errors()).extracting("field").containsExactly("longitude"));
  }

  @Test
  void fortySevenCountiesWithOfficialCodes() {
    County[] counties = County.values();
    assertThat(counties).hasSize(47);
    assertThat(Arrays.stream(counties).mapToInt(County::code).sorted().toArray())
        .containsExactly(java.util.stream.IntStream.rangeClosed(1, 47).toArray());
    assertThat(Arrays.stream(counties).map(County::label).distinct()).hasSize(47);
    assertThat(County.MOMBASA.code()).isEqualTo(1);
    assertThat(County.NAIROBI.code()).isEqualTo(47);
    assertThat(County.MURANGA.label()).isEqualTo("Murang'a");
    assertThat(County.TAITA_TAVETA.toString()).isEqualTo("Taita-Taveta");
  }
}
