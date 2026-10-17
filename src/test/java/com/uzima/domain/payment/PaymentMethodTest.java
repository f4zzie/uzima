package com.uzima.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PaymentMethodTest {

  private static void assertReferenceError(PaymentMethod method, String raw) {
    assertThatThrownBy(() -> method.normaliseReference(raw))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("reference")).isPresent());
  }

  @Test
  void acceptsValidMpesaCode() {
    assertThat(PaymentMethod.MPESA.normaliseReference("QJK3H5L7PZ")).isEqualTo("QJK3H5L7PZ");
  }

  @Test
  void normalisesMpesaCodeCaseAndWhitespace() {
    assertThat(PaymentMethod.MPESA.normaliseReference("  qjk3h5l7pz ")).isEqualTo("QJK3H5L7PZ");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "QJK3H5L7P", "QJK3H5L7PZX", "QJK3H5-7PZ", "QJK3H5 L7PZ"})
  void rejectsInvalidMpesaCodes(String raw) {
    assertReferenceError(PaymentMethod.MPESA, raw);
  }

  @ParameterizedTest
  @EnumSource(
      value = PaymentMethod.class,
      names = {"BANK_TRANSFER", "CHEQUE"})
  void bankAndChequeRequireReference(PaymentMethod method) {
    assertReferenceError(method, null);
    assertReferenceError(method, "  ");
    assertReferenceError(method, "x".repeat(PaymentMethod.MAX_REFERENCE + 1));
    assertThat(method.normaliseReference(" 000123 ")).isEqualTo("000123");
  }

  @Test
  void cashReferenceIsOptional() {
    assertThat(PaymentMethod.CASH.normaliseReference(null)).isEmpty();
    assertThat(PaymentMethod.CASH.normaliseReference("  ")).isEmpty();
    assertThat(PaymentMethod.CASH.normaliseReference(" till 4 ")).isEqualTo("till 4");
    assertReferenceError(PaymentMethod.CASH, "x".repeat(PaymentMethod.MAX_REFERENCE + 1));
  }

  @Test
  void labels() {
    assertThat(PaymentMethod.MPESA).hasToString("M-Pesa");
    assertThat(PaymentMethod.BANK_TRANSFER.label()).isEqualTo("Bank transfer");
    assertThat(PaymentStatus.PARTIALLY_PAID).hasToString("Partially paid");
  }
}
